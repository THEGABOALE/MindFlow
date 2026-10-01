package com.mindflow.nova.data.mission

import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.FinishAttemptRequest
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.connectionErrorMessage
import com.mindflow.nova.data.remote.httpErrorMessage

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

    suspend fun startAttempt(missionId: Int): AttemptStartResult

    /** El resultado ya corregido por el backend, con la racha, o null si no se pudo guardar. */
    suspend fun finishAttempt(attemptId: Int, answers: List<AnswerSubmission>, timedOut: Boolean): AttemptResult?
}

class RemoteMissionRepository(
    private val api: NovaApiService = RetrofitClient.api
) : MissionRepository {

    override suspend fun loadContent(missionId: Int): MissionContentResult =
        try {
            val response = api.getMissionContent(missionId)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                MissionContentResult.Loaded(body.mission)
            } else {
                MissionContentResult.Failed(
                    httpErrorMessage(response.code(), response.errorBody()?.string(), "No se pudo cargar la misión")
                )
            }
        } catch (e: Exception) {
            MissionContentResult.Failed(connectionErrorMessage(e))
        }

    override suspend fun startAttempt(missionId: Int): AttemptStartResult =
        try {
            val response = api.startAttempt(missionId)
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
            val response = api.finishAttempt(attemptId, FinishAttemptRequest(answers, timedOut))

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
}
