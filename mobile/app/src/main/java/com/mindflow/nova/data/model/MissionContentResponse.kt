package com.mindflow.nova.data.model

/**
 * Respuesta de GET /api/missions/:id — el contenido jugable de una misión.
 * Según la mecánica, cada pregunta trae `options` (opción múltiple y
 * verdadero/falso) o `pairs` (relación de conceptos).
 */
data class MissionContentResponse(
    val message: String?,
    val status: String?,
    val mission: MissionContent
)

data class MissionContent(
    val id: Int,
    val levelId: Int,
    val title: String,
    val description: String?,
    val topic: String?,
    val orderIndex: Int,
    val pointsReward: Int,
    val mechanic: String?,
    val timeLimitSeconds: Int?,
    val maxPlumas: Int?,
    val questions: List<MissionQuestion>
)

data class MissionQuestion(
    val id: Int,
    val prompt: String,
    val type: String,
    /** Explicación de la pregunta. En verdadero/falso es la que ve el estudiante. */
    val feedback: String?,
    val orderIndex: Int,
    val points: Int?,
    val options: List<MissionOption> = emptyList(),
    val pairs: List<MissionPair> = emptyList()
)

data class MissionOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val feedback: String?,
    val orderIndex: Int
)

data class MissionPair(
    val id: Int,
    val term: String,
    val match: String,
    val orderIndex: Int
)
