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
import com.mindflow.nova.data.model.StudentProgressResponse
import com.mindflow.nova.data.model.SyncAttemptsRequest
import com.mindflow.nova.data.model.SyncAttemptsResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

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

    /**
     * Sube intentos jugados en el teléfono: el servidor los vuelve a calificar
     * y responde, uno por uno, si los aceptó o por qué no.
     */
    @POST("api/sync/attempts")
    suspend fun syncAttempts(
        /** El de la cuenta dueña de los intentos, no el de la sesión de ese momento. */
        @Header("Authorization") authorization: String,
        @Body request: SyncAttemptsRequest
    ): Response<SyncAttemptsResponse>

    /**
     * Progreso acumulado del estudiante: semillas totales, misiones completadas, % por
     * nivel y racha. El desfase del huso es para que el backend cuente los dias de la
     * racha en la fecha local de la persona y no en la de su servidor.
     */
    @GET("api/students/{studentId}/progress")
    suspend fun getStudentProgress(
        @Path("studentId") studentId: Int,
        @Query("tzOffsetMinutes") tzOffsetMinutes: Int = currentTzOffsetMinutes()
    ): Response<StudentProgressResponse>

    @POST("api/auth/login/id")
    suspend fun loginWithId(@Body request: LoginIdRequest): Response<LoginResponse>

    @POST("api/auth/login/google")
    suspend fun loginWithGoogle(@Body request: LoginGoogleRequest): Response<LoginResponse>

    /** Valida el token guardado y dice quién es la persona y con qué rol. */
    @GET("api/auth/me")
    suspend fun getMe(): Response<MeResponse>
}
