package com.mindflow.nova.data.model

data class HealthResponse(
    val message: String,
    val status: String,
    val service: String? = null,
    val databaseTime: String? = null
)