package com.mindflow.nova.data.remote

import com.mindflow.nova.data.model.HealthResponse
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.JoinGroupRequest
import com.mindflow.nova.data.model.JoinGroupResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.GET

interface NovaApiService {
    @GET("/")
    suspend fun getHealth(): Response<HealthResponse>
    @GET("api/health/db")
    suspend fun getDatabaseHealth(): Response<HealthResponse>
    @GET("api/levels")
    suspend fun getLevels(): Response<List<LevelResponse>>

    @POST("api/groups/join")
    suspend fun joinGroupByCode(@Body request: JoinGroupRequest): Response<JoinGroupResponse>
}