package com.mindflow.nova.data.offline

import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.AttemptStreak
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.StudentStreak
import com.mindflow.nova.data.model.SyncProgress
import com.mindflow.nova.data.model.SyncedAttempt
import kotlinx.coroutines.CancellationException
import kotlin.math.abs

/** Cómo terminó el guardado de un intento, para la pantalla de cierre de la lección. */
sealed class LessonOutcome {
    /** El servidor lo aceptó: [result] es el oficial. */
    data class Synced(val result: AttemptResult) : LessonOutcome()

    /** Quedó guardado en el teléfono y se sube después: [result] es el calculado ahí. */
    data class Pending(val result: AttemptResult) : LessonOutcome()

    /** El servidor no lo aceptó (por ejemplo, la misión anterior no estaba completada). */
    data class Rejected(val message: String) : LessonOutcome()

    /** Ni siquiera se pudo guardar en el teléfono. */
    object SaveFailed : LessonOutcome()
}

/** Un intento recién terminado, con las horas del dispositivo. */
data class FinishedAttempt(
    val clientAttemptId: String,
    val content: MissionContent,
    val answers: List<AnswerSubmission>,
    val timedOut: Boolean,
    val startedAtMs: Long,
    val finishedAtMs: Long,
    val tzOffsetMinutes: Int
)

interface AttemptRecorder {
    suspend fun record(attempt: FinishedAttempt): LessonOutcome
}

/**
 * Guarda cada intento primero en el teléfono y después intenta subirlo. Así
 * un resultado nunca se pierde por falta de conexión: si no sube en el
 * momento, queda pendiente y se sube solo más tarde ([scheduleSync]).
 *
 * El resultado provisional usa las mismas reglas que el servidor, con lo que
 * se sabe en el teléfono: el último progreso descargado y lo que falta subir.
 */
class OfflineAttemptRecorder(
    private val local: LocalStore,
    private val sync: SyncRepository,
    private val currentUserId: () -> Int?,
    private val scheduleSync: () -> Unit = {},
    private val syncTimeoutMs: Long = 10_000
) : AttemptRecorder {

    override suspend fun record(attempt: FinishedAttempt): LessonOutcome {
        val userId = currentUserId() ?: return LessonOutcome.SaveFailed
        val provisional = try {
            provisionalResult(userId, attempt)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return LessonOutcome.SaveFailed
        }

        try {
            local.addPending(toPending(userId, attempt, provisional))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return LessonOutcome.SaveFailed
        }

        val report = sync.syncNow(userId, syncTimeoutMs)
        val id = attempt.clientAttemptId

        report?.accepted?.get(id)?.let { synced ->
            return LessonOutcome.Synced(synced.toResult(report.progress))
        }

        report?.rejected?.get(id)?.let { message ->
            // La pantalla de cierre ya lo cuenta: el Inicio no lo repite.
            sync.dismissNotice(id)
            return LessonOutcome.Rejected(message)
        }

        scheduleSync()
        return LessonOutcome.Pending(provisional)
    }

    private suspend fun provisionalResult(userId: Int, attempt: FinishedAttempt): AttemptResult {
        val missionId = attempt.content.id
        val official = local.progress(userId)
        val pending = local.pending(userId)
        val today = localDay(attempt.finishedAtMs, attempt.tzOffsetMinutes)

        val before = mergePending(official, pending, today)
        val isReview = before?.completedMissionIds?.contains(missionId) == true

        // Repasos de esta misión que ya pagaron cerca de este: en el servidor
        // también cuentan los suyos, acá solo se conocen los que faltan subir.
        val paidReviewsNearby = if (!isReview) 0 else pending.count {
            it.missionId == missionId && it.provisional.isReview && it.provisional.pointsEarned > 0 &&
                abs(epochFromIso(it.finishedAt) - attempt.finishedAtMs) < REVIEW_PAY_WINDOW_MS
        }

        val outcome = gradeLocally(
            content = attempt.content,
            answers = attempt.answers,
            timedOut = attempt.timedOut,
            elapsedSeconds = (attempt.finishedAtMs - attempt.startedAtMs) / 1000.0,
            isReview = isReview,
            paidReviewsNearby = paidReviewsNearby
        )

        val result = AttemptResult(
            id = 0,
            missionId = missionId,
            score = outcome.score,
            correctAnswers = outcome.correctAnswers,
            wrongAnswers = outcome.wrongAnswers,
            plumasLeft = outcome.plumasLeft,
            pointsEarned = outcome.pointsEarned,
            isReview = outcome.isReview,
            status = outcome.status
        )

        // La racha con este intento incluido. Lo enciende si es lo primero que se juega hoy.
        val after = mergePending(official, pending + toPending(userId, attempt, result), today)?.streak
        val streak = after?.let {
            AttemptStreak(days = it.days, isActive = it.isActive, justActivated = before?.streak?.playedOn(today) != true)
        }

        return result.copy(streak = streak)
    }

    private fun toPending(userId: Int, attempt: FinishedAttempt, result: AttemptResult) = PendingAttempt(
        clientAttemptId = attempt.clientAttemptId,
        userId = userId,
        missionId = attempt.content.id,
        startedAt = isoWithOffset(attempt.startedAtMs, attempt.tzOffsetMinutes),
        finishedAt = isoWithOffset(attempt.finishedAtMs, attempt.tzOffsetMinutes),
        tzOffsetMinutes = attempt.tzOffsetMinutes,
        timedOut = attempt.timedOut,
        answers = attempt.answers,
        provisional = result
    )

    private fun StudentStreak.playedOn(day: String) = lastActivityDate == day

    private fun SyncedAttempt.toResult(progress: SyncProgress?) = AttemptResult(
        id = 0,
        missionId = missionId,
        score = score,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        plumasLeft = plumasLeft,
        pointsEarned = pointsEarned,
        isReview = isReview,
        status = status,
        streak = progress?.streak?.let {
            AttemptStreak(days = it.days, isActive = it.isActive, justActivated = progress.justActivatedStreak)
        }
    )
}
