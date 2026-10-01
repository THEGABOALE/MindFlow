package com.mindflow.nova.data.mission

import com.mindflow.nova.AppServices
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.local.localOrNull
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.FinishAttemptRequest
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.connectionErrorMessage
import com.mindflow.nova.data.remote.httpErrorMessage
import kotlinx.coroutines.CancellationException

/** Resultado de pedir el contenido jugable de una misión. */
sealed class MissionContentResult {
    data class Loaded(val content: MissionContent) : MissionContentResult()
    data class Failed(val message: String) : MissionContentResult()
}

/** Resultado de abrir un intento en el backend. */
sealed class AttemptStartResult {
    data class Started(val attemptId: Int) : AttemptStartResult()
    data class Failed(val message: String) : AttemptStartResult()
}

/**
 * Contenido de las misiones y sus intentos. Las pantallas de lección no llaman
 * a la API; se lo piden al ViewModel, y el ViewModel a este repositorio.
 */
interface MissionRepository {
    suspend fun loadContent(missionId: Int): MissionContentResult

    /**
     * Baja y guarda las misiones que todavía no estén en el teléfono o que se
     * guardaron hace más de 24 h, para poder jugarlas sin conexión.
     */
    suspend fun prefetch(missionIds: List<Int>)

    suspend fun startAttempt(missionId: Int): AttemptStartResult

    /** El resultado ya corregido por el backend, con la racha, o null si no se pudo guardar. */
    suspend fun finishAttempt(attemptId: Int, answers: List<AnswerSubmission>, timedOut: Boolean): AttemptResult?
}

/** El contenido se pide al servidor y se guarda; sin respuesta, se juega la copia guardada. */
class RemoteMissionRepository(
    private val api: () -> NovaApiService = { RetrofitClient.api },
    private val local: LocalStore = AppServices.localStore,
    private val now: () -> Long = System::currentTimeMillis
) : MissionRepository {

    override suspend fun loadContent(missionId: Int): MissionContentResult {
        val failure = try {
            val response = api().getMissionContent(missionId)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                localOrNull { local.saveMission(body.mission) }
                return MissionContentResult.Loaded(body.mission)
            }

            val message = httpErrorMessage(response.code(), response.errorBody()?.string(), "No se pudo cargar la misión")
            // La misión ya no existe o no está publicada: la copia guardada no sirve.
            if (response.code() == 404) return MissionContentResult.Failed(message)
            message
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NOT_DOWNLOADED_MESSAGE
        }

        val cached = localOrNull { local.mission(missionId) }

        return if (cached != null) MissionContentResult.Loaded(cached) else MissionContentResult.Failed(failure)
    }

    override suspend fun prefetch(missionIds: List<Int>) {
        for (missionId in missionIds.distinct()) {
            val savedAt = localOrNull { local.missionSavedAt(missionId) }
            if (savedAt != null && now() - savedAt < MAX_AGE_MS) continue

            // Una que falle no corta las demás; se vuelve a intentar en la próxima carga.
            try {
                val response = api().getMissionContent(missionId)
                val body = response.body()
                if (response.isSuccessful && body != null) localOrNull { local.saveMission(body.mission) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                continue
            }
        }
    }

    override suspend fun startAttempt(missionId: Int): AttemptStartResult =
        try {
            val response = api().startAttempt(missionId)
            val attempt = response.body()?.attempt

            if (response.isSuccessful && attempt != null) {
                AttemptStartResult.Started(attempt.id)
            } else {
                // Si el backend no deja abrirla (otro nivel, falta la anterior) el
                // motivo viene en el cuerpo del error y se muestra tal cual.
                AttemptStartResult.Failed(
                    httpErrorMessage(response.code(), response.errorBody()?.string(), "No se pudo empezar la misión")
                )
            }
        } catch (e: Exception) {
            AttemptStartResult.Failed(connectionErrorMessage(e))
        }

    override suspend fun finishAttempt(
        attemptId: Int,
        answers: List<AnswerSubmission>,
        timedOut: Boolean
    ): AttemptResult? =
        try {
            val response = api().finishAttempt(attemptId, FinishAttemptRequest(answers, timedOut))

            if (response.isSuccessful) {
                // La racha viene al lado del intento en la respuesta; se la pega al
                // resultado para que la pantalla de cierre la tenga a mano.
                response.body()?.let { body -> body.attempt.copy(streak = body.streak) }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    private companion object {
        const val MAX_AGE_MS = 24L * 60 * 60 * 1000
        const val NOT_DOWNLOADED_MESSAGE = "Esta misión todavía no está descargada. Conéctate a internet para bajarla."
    }
}
