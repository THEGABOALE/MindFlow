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
    /** Qué minijuego es: multiple_choice, matching, true_false, word_search. */
    val mechanic: String?,
    /** Segundos límite de la misión, o null si no tiene contrarreloj. */
    val timeLimitSeconds: Int?,
    /** Plumas disponibles antes de perder la misión. */
    val maxPlumas: Int?,
    val isPublished: Boolean
)