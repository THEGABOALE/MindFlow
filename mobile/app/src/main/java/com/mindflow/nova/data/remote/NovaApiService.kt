package com.mindflow.nova.data.remote

import com.mindflow.nova.data.model.HealthResponse
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.JoinGroupRequest
import com.mindflow.nova.data.model.JoinGroupResponse
import com.mindflow.nova.data.model.LoginGoogleRequest
import com.mindflow.nova.data.model.LoginIdRequest
import com.mindflow.nova.data.model.LoginResponse
import com.mindflow.nova.data.model.MeResponse
import com.mindflow.nova.data.model.MissionContentResponse
import com.mindflow.nova.data.model.FinishAttemptRequest
import com.mindflow.nova.data.model.FinishAttemptResponse
import com.mindflow.nova.data.model.StartAttemptResponse
import com.mindflow.nova.data.model.StudentProgressResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface NovaApiService {
    @GET("/")
    suspend fun getHealth(): Response<HealthResponse>
    @GET("api/health/db")
    suspend fun getDatabaseHealth(): Response<HealthResponse>
    @GET("api/levels")
    suspend fun getLevels(): Response<List<LevelResponse>>

    @POST("api/groups/join")
    suspend fun joinGroupByCode(@Body request: JoinGroupRequest): Response<JoinGroupResponse>

    /** Contenido jugable de una misión: preguntas con sus opciones o pares. */
    @GET("api/missions/{missionId}")
    suspend fun getMissionContent(@Path("missionId") missionId: Int): Response<MissionContentResponse>

    /** Abre un intento nuevo (o de repaso, si ya la completó antes) de una misión. */
    @POST("api/missions/{missionId}/attempts")
    suspend fun startAttempt(@Path("missionId") missionId: Int): Response<StartAttemptResponse>

    /** Cierra un intento: el backend corrige, puntúa y actualiza el progreso del nivel. */
    @POST("api/missions/attempts/{attemptId}/finish")
    suspend fun finishAttempt(
        @Path("attemptId") attemptId: Int,
        @Body request: FinishAttemptRequest
    ): Response<FinishAttemptResponse>

    /** Progreso acumulado del estudiante: semillas totales, misiones completadas y % por nivel. */
    @GET("api/students/{studentId}/progress")
    suspend fun getStudentProgress(@Path("studentId") studentId: Int): Response<StudentProgressResponse>

    @POST("api/auth/login/id")
    suspend fun loginWithId(@Body request: LoginIdRequest): Response<LoginResponse>

    @POST("api/auth/login/google")
    suspend fun loginWithGoogle(@Body request: LoginGoogleRequest): Response<LoginResponse>

    /** Valida el token guardado y dice quién es la persona y con qué rol. */
    @GET("api/auth/me")
    suspend fun getMe(): Response<MeResponse>
}
