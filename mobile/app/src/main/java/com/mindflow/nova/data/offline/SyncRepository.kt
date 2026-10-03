package com.mindflow.nova.data.offline

import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.model.SyncAttemptItem
import com.mindflow.nova.data.model.SyncAttemptsRequest
import com.mindflow.nova.data.model.SyncProgress
import com.mindflow.nova.data.model.SyncedAttempt
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.currentTzOffsetMinutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

/** Por qué se cortó una subida antes de mandar todo. */
enum class SyncStop {
    /** No hubo conexión: se reintenta cuando vuelva la red. */
    NETWORK,
    /** El servidor falló o respondió algo inesperado: se reintenta más tarde. */
    SERVER,
    /** La sesión venció (401/403): hay que volver a iniciar sesión para subir. */
    NEEDS_LOGIN
}

/**
 * Lo que pasó en una subida. Las claves son los clientAttemptId de la cola;
 * [rejected] trae el motivo que dio el servidor. Si [stoppedBy] no es null,
 * lo que no aparece en ninguno de los dos sigue en la cola.
 */
data class SyncReport(
    val accepted: Map<String, SyncedAttempt>,
    val rejected: Map<String, String>,
    val progress: SyncProgress?,
    val stoppedBy: SyncStop?
)

/** Un intento que el servidor no aceptó y por qué, para avisarlo en el Inicio. */
data class RejectedNotice(val clientAttemptId: String, val message: String)

/**
 * Sube los intentos jugados en el teléfono con POST /api/sync/attempts. Los
 * aceptados y los rechazados salen de la cola; con cualquier otro problema se
 * corta y lo que falta queda para el próximo intento. Repetir una subida no
 * duplica nada: el servidor reconoce cada intento por su clientAttemptId.
 *
 * La usan el cierre de una lección (con [syncNow]) y el trabajo en segundo
 * plano, por eso es una sola para toda la app.
 *
 * Cada subida usa el token de la cuenta dueña de la cola, tomado al empezar
 * ([tokenFor] da null si la sesión ya no es de esa cuenta). Si se cambia de
 * cuenta a mitad de camino, lo que falta se sigue mandando como de la primera.
 */
class SyncRepository(
    private val api: () -> NovaApiService,
    private val store: LocalStore,
    private val tokenFor: (userId: Int) -> String?,
    private val tzOffset: () -> Int = ::currentTzOffsetMinutes
) {
    // Dos subidas a la vez leerían la misma cola y mandarían dos veces lo mismo.
    private val mutex = Mutex()

    /**
     * Intentos de [userId] que el servidor no aceptó, para avisar en el Inicio.
     * Quedan guardados en el teléfono: un rechazo que llega en segundo plano
     * se ve aunque la app se haya cerrado antes de abrir el Inicio.
     */
    fun rejectedNotices(userId: Int): Flow<List<RejectedNotice>> = store.observeNotices(userId)

    /** Ya los vio: se borran. */
    suspend fun consumeNotices(userId: Int) = store.clearNotices(userId)

    /** Quita el aviso de un intento cuyo rechazo ya se mostró (al cerrar la lección). */
    suspend fun dismissNotice(clientAttemptId: String) = store.removeNotice(clientAttemptId)

    suspend fun syncPending(userId: Int): SyncReport = mutex.withLock {
        val accepted = mutableMapOf<String, SyncedAttempt>()
        val rejected = mutableMapOf<String, String>()
        var progress: SyncProgress? = null

        val pending = store.pending(userId)
        if (pending.isEmpty()) return@withLock SyncReport(accepted, rejected, progress, stoppedBy = null)

        // Sin sesión de esta cuenta no se manda nada: con otro token los
        // intentos quedarían a nombre de otra persona.
        val token = tokenFor(userId)
            ?: return@withLock SyncReport(accepted, rejected, progress, SyncStop.NEEDS_LOGIN)

        for (batch in pending.chunked(MAX_BATCH)) {
            val stop = sendBatch(userId, token, batch, accepted, rejected) { progress = it }

            if (stop != null) return@withLock SyncReport(accepted, rejected, progress, stop)
        }

        SyncReport(accepted, rejected, progress, stoppedBy = null)
    }

    /** [syncPending] con tiempo máximo, para no dejar esperando al cerrar una lección. Null si no alcanzó. */
    suspend fun syncNow(userId: Int, timeoutMs: Long = 10_000): SyncReport? =
        withTimeoutOrNull(timeoutMs) { syncPending(userId) }

    private suspend fun sendBatch(
        userId: Int,
        token: String,
        batch: List<PendingAttempt>,
        accepted: MutableMap<String, SyncedAttempt>,
        rejected: MutableMap<String, String>,
        onProgress: (SyncProgress) -> Unit
    ): SyncStop? {
        val request = SyncAttemptsRequest(
            tzOffsetMinutes = tzOffset(),
            attempts = batch.map {
                SyncAttemptItem(
                    it.clientAttemptId, it.missionId, it.startedAt, it.finishedAt, it.timedOut, it.answers,
                    pausedSeconds = it.pausedSeconds, usedExtraTime = it.usedExtraTime
                )
            }
        )

        val response = try {
            api().syncAttempts("Bearer $token", request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            return SyncStop.NETWORK
        } catch (e: Exception) {
            // Una respuesta que no se pudo leer: mejor reintentar que perder intentos.
            return SyncStop.SERVER
        }

        if (response.code() == 401 || response.code() == 403) return SyncStop.NEEDS_LOGIN

        val body = response.body()
        if (!response.isSuccessful || body == null) return SyncStop.SERVER

        // El servidor guarda los ids en minúsculas; se buscan sin importar mayúsculas.
        val byId = batch.associateBy { it.clientAttemptId.lowercase() }
        val done = mutableListOf<String>()
        val notices = mutableListOf<RejectedNotice>()

        for (result in body.results) {
            val id = byId[result.clientAttemptId.lowercase()]?.clientAttemptId ?: continue

            when {
                result.status == "accepted" && result.attempt != null -> accepted[id] = result.attempt
                result.status == "rejected" -> {
                    val message = result.message ?: "No se pudo guardar uno de tus resultados"
                    rejected[id] = message
                    notices += RejectedNotice(id, message)
                }
                else -> continue
            }
            done += id
        }

        store.settlePending(userId, done, notices)

        body.progress?.let { synced ->
            onProgress(synced)
            // Si todavía no hay progreso guardado no hay de dónde sacar el resto
            // (niveles, nombre): se espera a la próxima carga del Inicio.
            store.progress(userId)?.let { saved ->
                store.saveProgress(
                    userId,
                    saved.copy(
                        totalPoints = synced.totalPoints,
                        missionsCompleted = synced.missionsCompleted,
                        completedMissionIds = synced.completedMissionIds,
                        streak = synced.streak ?: saved.streak
                    )
                )
            }
        }

        return null
    }

    companion object {
        /** El servidor no acepta más de 50 intentos por llamada. */
        const val MAX_BATCH = 50
    }
}
