package com.mindflow.nova.data.model

data class LevelResponse(
    val id: Int,
    val name: String,
    val code: String,
    val description: String?,
    val orderIndex: Int,
    val missions: List<MissionResponse>
)

data class MissionResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val topic: String?,
    val orderIndex: Int,
    val pointsReward: Int,
    val isPublished: Boolean
)