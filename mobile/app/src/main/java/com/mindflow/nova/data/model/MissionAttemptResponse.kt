package com.mindflow.nova.data.model

/**
 * Una respuesta al cerrar un intento. Opción múltiple y verdadero/falso usan
 * `selectedOptionId`; relación de conceptos usa `pairId` (el término que se
 * preguntaba) y `selectedPairId` (con qué palabra lo emparejó el estudiante).
 */
data class AnswerSubmission(
    val questionId: Int,
    val selectedOptionId: Int? = null,
    val pairId: Int? = null,
    val selectedPairId: Int? = null
)

data class FinishAttemptRequest(
    val answers: List<AnswerSubmission>,
    val timedOut: Boolean
)

data class StartAttemptResponse(
    val message: String?,
    val status: String?,
    val attempt: AttemptStart
)

data class AttemptStart(
    val id: Int,
    val missionId: Int,
    val isReview: Boolean,
    val maxPlumas: Int?,
    val timeLimitSeconds: Int?,
    val startedAt: String?
)

data class FinishAttemptResponse(
    val message: String?,
    val status: String?,
    val attempt: AttemptResult,
    val levelProgress: LevelProgressResult?
)

data class AttemptResult(
    val id: Int,
    val missionId: Int,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val plumasLeft: Int,
    val pointsEarned: Int,
    val isReview: Boolean,
    /** "completed" o "failed", segun lo decidio el backend. */
    val status: String
)

data class LevelProgressResult(
    val levelId: Int,
    val progressPercentage: Double,
    val status: String
)
