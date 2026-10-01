package com.mindflow.nova.data.remote

import com.mindflow.nova.data.model.FinishAttemptRequest
import com.mindflow.nova.data.model.FinishAttemptResponse
import com.mindflow.nova.data.model.HealthResponse
import com.mindflow.nova.data.model.JoinGroupRequest
import com.mindflow.nova.data.model.JoinGroupResponse
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.LoginGoogleRequest
import com.mindflow.nova.data.model.LoginIdRequest
import com.mindflow.nova.data.model.LoginResponse
import com.mindflow.nova.data.model.MeResponse
import com.mindflow.nova.data.model.MissionContentResponse
import com.mindflow.nova.data.model.StartAttemptResponse
import com.mindflow.nova.data.model.StudentProgressResponse
import com.mindflow.nova.data.model.SyncAttemptResult
import com.mindflow.nova.data.model.SyncAttemptsRequest
import com.mindflow.nova.data.model.SyncAttemptsResponse
import com.mindflow.nova.data.model.SyncProgress
import com.mindflow.nova.data.model.SyncedAttempt
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

/**
 * API falsa para los tests: cada endpoint responde con lo que se le asigne y
 * guarda lo que recibió. Lo que un test no configura lanza NotImplementedError.
 */
class FakeNovaApi : NovaApiService {

    val syncRequests = mutableListOf<SyncAttemptsRequest>()

    /** Por defecto acepta todo con un resultado completo y sin progreso. */
    var onSync: suspend (SyncAttemptsRequest) -> Response<SyncAttemptsResponse> = { request ->
        Response.success(
            SyncAttemptsResponse(
                message = null,
                status = "OK",
                results = request.attempts.map { acceptedResult(it.clientAttemptId, it.missionId) },
                progress = null
            )
        )
    }

    override suspend fun syncAttempts(request: SyncAttemptsRequest): Response<SyncAttemptsResponse> {
        syncRequests += request
        return onSync(request)
    }

    var onMe: suspend () -> Response<MeResponse> = { TODO() }
    var onLoginId: suspend (LoginIdRequest) -> Response<LoginResponse> = { TODO() }
    var onLoginGoogle: suspend (LoginGoogleRequest) -> Response<LoginResponse> = { TODO() }

    override suspend fun getMe(): Response<MeResponse> = onMe()
    override suspend fun loginWithId(request: LoginIdRequest): Response<LoginResponse> = onLoginId(request)
    override suspend fun loginWithGoogle(request: LoginGoogleRequest): Response<LoginResponse> = onLoginGoogle(request)

    var onLevels: suspend () -> Response<List<LevelResponse>> = { TODO() }
    var onProgress: suspend (Int) -> Response<StudentProgressResponse> = { TODO() }
    val missionRequests = mutableListOf<Int>()
    var onMissionContent: suspend (Int) -> Response<MissionContentResponse> = { TODO() }

    override suspend fun getLevels(): Response<List<LevelResponse>> = onLevels()
    override suspend fun getStudentProgress(studentId: Int, tzOffsetMinutes: Int): Response<StudentProgressResponse> =
        onProgress(studentId)
    override suspend fun getMissionContent(missionId: Int): Response<MissionContentResponse> {
        missionRequests += missionId
        return onMissionContent(missionId)
    }

    override suspend fun getHealth(): Response<HealthResponse> = TODO()
    override suspend fun getDatabaseHealth(): Response<HealthResponse> = TODO()
    override suspend fun joinGroupByCode(request: JoinGroupRequest): Response<JoinGroupResponse> = TODO()
    override suspend fun startAttempt(missionId: Int): Response<StartAttemptResponse> = TODO()
    override suspend fun finishAttempt(attemptId: Int, request: FinishAttemptRequest): Response<FinishAttemptResponse> = TODO()

    companion object {
        fun acceptedResult(clientAttemptId: String, missionId: Int, pointsEarned: Int = 100) = SyncAttemptResult(
            clientAttemptId = clientAttemptId,
            status = "accepted",
            attempt = SyncedAttempt(
                missionId = missionId, score = 100, correctAnswers = 3, wrongAnswers = 0, plumasLeft = 3,
                pointsEarned = pointsEarned, isReview = false, status = "completed"
            )
        )

        fun rejectedResult(clientAttemptId: String, message: String) =
            SyncAttemptResult(clientAttemptId = clientAttemptId, status = "rejected", message = message)

        fun syncOk(results: List<SyncAttemptResult>, progress: SyncProgress? = null): Response<SyncAttemptsResponse> =
            Response.success(SyncAttemptsResponse(message = null, status = "OK", results = results, progress = progress))

        fun <T> httpError(code: Int): Response<T> = Response.error(code, "{\"message\":\"x\"}".toResponseBody())
    }
}
