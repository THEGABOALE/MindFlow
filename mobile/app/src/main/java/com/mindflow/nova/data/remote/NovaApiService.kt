package com.mindflow.nova.data.remote

import com.mindflow.nova.data.model.HealthResponse
import retrofit2.Response
import retrofit2.http.GET

interface NovaApiService {
    @GET("/")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/health/db")
    suspend fun getDatabaseHealth(): Response<HealthResponse>
}