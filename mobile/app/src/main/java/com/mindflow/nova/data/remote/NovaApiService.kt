package com.mindflow.nova.data.remote

import com.mindflow.nova.data.model.HealthResponse
import com.mindflow.nova.data.model.LevelResponse
import retrofit2.Response
import retrofit2.http.GET

interface NovaApiService {
    @GET("/")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/health/db")
    suspend fun getDatabaseHealth(): Response<HealthResponse>

    @GET("api/levels")
    suspend fun getLevels(): Response<List<LevelResponse>>
}