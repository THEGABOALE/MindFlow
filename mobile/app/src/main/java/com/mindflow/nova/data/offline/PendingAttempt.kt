package com.mindflow.nova.data.offline

import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult

/**
 * Un intento terminado en el teléfono que todavía no subió al servidor.
 * [startedAt] y [finishedAt] son ISO con el huso del dispositivo
 * ("2026-10-01T14:03:52.000-06:00"); [provisional] es el resultado calculado
 * en el teléfono, que se muestra hasta que llega el oficial.
 */
data class PendingAttempt(
    val clientAttemptId: String,
    val userId: Int,
    val missionId: Int,
    val startedAt: String,
    val finishedAt: String,
    val tzOffsetMinutes: Int,
    val timedOut: Boolean,
    val answers: List<AnswerSubmission>,
    val provisional: AttemptResult
)
