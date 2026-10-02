package com.mindflow.nova.data.local

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.PendingAttempt
import com.mindflow.nova.data.offline.RejectedNotice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Lo que la app guarda en el teléfono. Todo lo de una persona va con su
 * userId, así que en una tablet compartida nadie ve los datos de otro.
 */
interface LocalStore {
    suspend fun saveUser(user: SessionUser)
    suspend fun user(userId: Int): SessionUser?

    suspend fun saveLevels(userId: Int, levels: List<LevelResponse>)
    suspend fun levels(userId: Int): List<LevelResponse>?

    suspend fun saveMission(content: MissionContent)
    suspend fun mission(missionId: Int): MissionContent?
    /** Cuándo se guardó la misión (epoch ms), o null si no está. */
    suspend fun missionSavedAt(missionId: Int): Long?

    suspend fun saveProgress(userId: Int, progress: StudentProgress)
    suspend fun progress(userId: Int): StudentProgress?
    fun observeProgress(userId: Int): Flow<StudentProgress?>

    suspend fun addPending(attempt: PendingAttempt)
    suspend fun pending(userId: Int): List<PendingAttempt>
    fun observePending(userId: Int): Flow<List<PendingAttempt>>
    /**
     * Saca de la cola de [userId] lo que el servidor ya resolvió y guarda los
     * avisos de los rechazados, en una sola escritura: si la app se cierra en
     * el medio, no queda un intento borrado sin su aviso.
     */
    suspend fun settlePending(userId: Int, doneIds: List<String>, notices: List<RejectedNotice>)
    fun observeNotices(userId: Int): Flow<List<RejectedNotice>>
    suspend fun clearNotices(userId: Int)
    suspend fun removeNotice(clientAttemptId: String)

    /** Borra la caché de la cuenta al cerrar sesión; los pendientes y sus avisos se quedan. */
    suspend fun clearAccount(userId: Int)
}

/**
 * Lo guardado es un respaldo: si leerlo o escribirlo falla, la pantalla sigue
 * con lo que vino del servidor (o con su error de siempre) en vez de romperse.
 */
internal suspend fun <T> localOrNull(block: suspend () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
