package com.mindflow.nova.data.model

import com.mindflow.nova.data.remote.currentTzOffsetMinutes

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
    val timedOut: Boolean,
    /** Huso del dispositivo, para que el backend cuente el día de la racha en la fecha local. */
    val tzOffsetMinutes: Int = currentTzOffsetMinutes()
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
    val levelProgress: LevelProgressResult?,
    val streak: AttemptStreak? = null
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
    val status: String,
    /** La racha después de cerrar el intento; la pone la app desde la respuesta, no viene dentro de `attempt`. */
    val streak: AttemptStreak? = null
)

data class LevelProgressResult(
    val levelId: Int,
    val progressPercentage: Double,
    val status: String
)

data class AttemptStreak(
    val days: Int,
    val isActive: Boolean,
    /** True si este fue el primer intento del día: el que encendió o descongeló la racha. */
    val justActivated: Boolean
)
