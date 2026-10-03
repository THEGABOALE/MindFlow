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

data class AttemptResult(
    val id: Int,
    val missionId: Int,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val plumasLeft: Int,
    val pointsEarned: Int,
    val isReview: Boolean,
    /** "completed" o "failed". */
    val status: String,
    /** La racha después de este intento: la del servidor si ya se subió, o la calculada en el teléfono. */
    val streak: AttemptStreak? = null,
    /** Semillas gastadas en el potenciador "+30 s" durante el intento. */
    val seedsSpent: Int = 0
)

data class AttemptStreak(
    val days: Int,
    val isActive: Boolean,
    /** True si este fue el primer intento del día: el que encendió o descongeló la racha. */
    val justActivated: Boolean
)
