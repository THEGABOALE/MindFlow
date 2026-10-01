package com.mindflow.nova.data.model

/**
 * Cuerpo de POST /api/sync/attempts: intentos jugados en el teléfono, con sus
 * horas en el huso del dispositivo. [tzOffsetMinutes] es el huso de ahora,
 * para que el servidor cuente la racha en la fecha local de la persona.
 */
data class SyncAttemptsRequest(
    val tzOffsetMinutes: Int,
    val attempts: List<SyncAttemptItem>
)

data class SyncAttemptItem(
    /** UUID creado en el teléfono: si el mismo intento llega dos veces, el servidor no lo duplica. */
    val clientAttemptId: String,
    val missionId: Int,
    val startedAt: String,
    val finishedAt: String,
    val timedOut: Boolean,
    val answers: List<AnswerSubmission>
)

data class SyncAttemptsResponse(
    val message: String?,
    val status: String?,
    val results: List<SyncAttemptResult>,
    val progress: SyncProgress?
)

data class SyncAttemptResult(
    val clientAttemptId: String,
    /** "accepted" o "rejected". */
    val status: String,
    /** Por qué se rechazó, ya escrito para mostrarse. */
    val message: String? = null,
    /** El resultado oficial, solo si se aceptó. */
    val attempt: SyncedAttempt? = null
)

data class SyncedAttempt(
    val missionId: Int,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val plumasLeft: Int,
    val pointsEarned: Int,
    val isReview: Boolean,
    /** "completed" o "failed". */
    val status: String
)

/** El progreso del estudiante después de guardar el lote. */
data class SyncProgress(
    val totalPoints: Int,
    val missionsCompleted: Int,
    val completedMissionIds: List<Int> = emptyList(),
    val streak: StudentStreak? = null,
    /** True si el primer intento de hoy llegó en este lote: encendió o descongeló la racha. */
    val justActivatedStreak: Boolean = false
)
