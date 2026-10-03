package com.mindflow.nova.data.offline

import com.mindflow.nova.data.local.FakeLocalStore
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.AttemptStreak
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionOption
import com.mindflow.nova.data.model.MissionQuestion
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.model.StudentStreak
import com.mindflow.nova.data.model.SyncProgress
import com.mindflow.nova.data.model.SyncedAttempt
import com.mindflow.nova.data.model.SyncAttemptResult
import com.mindflow.nova.data.remote.FakeNovaApi
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.rejectedResult
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.syncOk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AttemptRecorderTest {

    private val userId = 7
    private val local = FakeLocalStore()
    private val api = FakeNovaApi()
    private val sync = SyncRepository(api = { api }, store = local, tokenFor = { "token" }, tzOffset = { -360 })
    private var scheduled = 0
    private fun recorder(store: LocalStore = local, user: Int? = userId) = OfflineAttemptRecorder(
        local = store, sync = sync, currentUserId = { user }, scheduleSync = { scheduled++ }
    )

    // 2026-10-01 12:26:04 en Costa Rica.
    private val startedAt = 1790879164000L
    private val hourMs = 60 * 60 * 1000L

    private fun question(id: Int) = MissionQuestion(
        id = id, prompt = "P$id", type = "multiple_choice", feedback = null, orderIndex = id, points = 1,
        options = listOf(
            MissionOption(id * 10, "Bien", true, null, 1),
            MissionOption(id * 10 + 1, "Mal", false, null, 2)
        )
    )

    private val content = MissionContent(
        id = 3, levelId = 1, title = "Misión 3", description = null, topic = null, orderIndex = 3,
        pointsReward = 100, mechanic = "multiple_choice", timeLimitSeconds = null, maxPlumas = 3,
        questions = listOf(question(1), question(2))
    )

    private val perfect = listOf(AnswerSubmission(1, selectedOptionId = 10), AnswerSubmission(2, selectedOptionId = 20))

    private fun finished(id: String = "nuevo", finishedAt: Long = startedAt + 60_000) = FinishedAttempt(
        clientAttemptId = id, content = content, answers = perfect, timedOut = false,
        startedAtMs = startedAt, finishedAtMs = finishedAt, tzOffsetMinutes = -360
    )

    private fun officialProgress(
        completed: List<Int> = emptyList(),
        streak: StudentStreak? = StudentStreak(3, false, "2026-09-30")
    ) = StudentProgress(userId, "Ana", 0, completed.size, completed, streak, emptyList())

    private fun pendingReview(id: String, finishedAt: Long, points: Int = 50) = PendingAttempt(
        clientAttemptId = id, userId = userId, missionId = 3,
        startedAt = isoWithOffset(finishedAt - 60_000, -360), finishedAt = isoWithOffset(finishedAt, -360),
        tzOffsetMinutes = -360, timedOut = false, answers = perfect,
        provisional = AttemptResult(0, 3, 100, 2, 0, 3, points, true, "completed")
    )

    private fun offline() {
        api.onSync = { throw IOException("sin red") }
    }

    @Test
    fun `el intento se guarda en el telefono antes de subirlo`() = runTest {
        var seenBeforeUpload: List<PendingAttempt> = emptyList()
        api.onSync = { request ->
            seenBeforeUpload = local.pendingNow(userId)
            syncOk(request.attempts.map { FakeNovaApi.acceptedResult(it.clientAttemptId, it.missionId) })
        }

        recorder().record(finished())

        val saved = seenBeforeUpload.single()
        assertEquals("nuevo", saved.clientAttemptId)
        assertEquals(3, saved.missionId)
        assertEquals("2026-10-01T12:26:04.000-06:00", saved.startedAt)
        assertEquals("2026-10-01T12:27:04.000-06:00", saved.finishedAt)
        assertEquals(perfect, saved.answers)
        assertEquals(100, saved.provisional.pointsEarned)
    }

    @Test
    fun `si se sube queda el resultado oficial con la racha del servidor`() = runTest {
        val streak = StudentStreak(4, true, "2026-10-01")
        api.onSync = {
            syncOk(
                listOf(
                    SyncAttemptResult(
                        clientAttemptId = "nuevo", status = "accepted",
                        attempt = SyncedAttempt(3, 100, 2, 0, 3, 90, false, "completed")
                    )
                ),
                SyncProgress(totalPoints = 90, missionsCompleted = 1, completedMissionIds = listOf(3), streak = streak, justActivatedStreak = true)
            )
        }

        val outcome = recorder().record(finished()) as LessonOutcome.Synced

        assertEquals(90, outcome.result.pointsEarned)
        assertEquals(AttemptStreak(days = 4, isActive = true, justActivated = true), outcome.result.streak)
        assertTrue(local.pendingNow(userId).isEmpty())
        assertEquals(0, scheduled)
    }

    @Test
    fun `sin red queda pendiente con el resultado del telefono`() = runTest {
        offline()

        val outcome = recorder().record(finished()) as LessonOutcome.Pending

        assertEquals("completed", outcome.result.status)
        assertEquals(100, outcome.result.pointsEarned)
        assertEquals(listOf("nuevo"), local.pendingNow(userId).map { it.clientAttemptId })
        assertEquals(1, scheduled)
    }

    @Test
    fun `si el servidor lo rechaza se muestra el motivo una sola vez`() = runTest {
        api.onSync = { syncOk(listOf(rejectedResult("nuevo", "Primero completa la misión anterior"))) }

        val outcome = recorder().record(finished())

        assertEquals(LessonOutcome.Rejected("Primero completa la misión anterior"), outcome)
        assertTrue(local.pendingNow(userId).isEmpty())
        // Ya lo vio al cerrar la lección: el Inicio no lo repite.
        assertTrue(sync.rejectedNotices(userId).first().isEmpty())
    }

    @Test
    fun `una mision ya completada cuenta como repaso`() = runTest {
        offline()
        local.saveProgress(userId, officialProgress(completed = listOf(3)))

        val outcome = recorder().record(finished()) as LessonOutcome.Pending

        assertTrue(outcome.result.isReview)
        assertEquals(50, outcome.result.pointsEarned)
    }

    @Test
    fun `una mision completada sin conexion tambien cuenta como repaso`() = runTest {
        offline()
        local.saveProgress(userId, officialProgress())
        local.addPending(
            pendingReview("antes", startedAt - hourMs).let {
                it.copy(provisional = it.provisional.copy(isReview = false, pointsEarned = 100))
            }
        )

        val outcome = recorder().record(finished()) as LessonOutcome.Pending

        assertTrue(outcome.result.isReview)
        assertEquals(50, outcome.result.pointsEarned)
    }

    @Test
    fun `cada repaso pendiente cerca parte a la mitad lo que paga`() = runTest {
        offline()
        local.saveProgress(userId, officialProgress(completed = listOf(3)))
        local.addPending(pendingReview("r1", startedAt - 2 * hourMs, points = 50))
        local.addPending(pendingReview("r2", startedAt - hourMs, points = 25))
        // Fuera de la ventana de 24 h: no cuenta.
        local.addPending(pendingReview("viejo", startedAt - 30 * hourMs, points = 50))
        // Un repaso que no pagó tampoco cuenta.
        local.addPending(pendingReview("sin-pago", startedAt - 3 * hourMs, points = 0))

        val outcome = recorder().record(finished()) as LessonOutcome.Pending

        // 100 * 0.5^3 = 12.5 -> 13
        assertEquals(13, outcome.result.pointsEarned)
    }

    @Test
    fun `la primera mision del dia enciende la racha provisional`() = runTest {
        offline()
        local.saveProgress(userId, officialProgress(streak = StudentStreak(3, false, "2026-09-30")))

        val outcome = recorder().record(finished()) as LessonOutcome.Pending

        assertEquals(AttemptStreak(days = 4, isActive = true, justActivated = true), outcome.result.streak)
    }

    @Test
    fun `si ya habia jugado hoy la racha no se vuelve a encender`() = runTest {
        offline()
        local.saveProgress(userId, officialProgress(streak = StudentStreak(3, false, "2026-09-30")))
        local.addPending(pendingReview("hoy", startedAt - 60_000))

        val outcome = recorder().record(finished()) as LessonOutcome.Pending

        assertEquals(4, outcome.result.streak!!.days)
        assertFalse(outcome.result.streak!!.justActivated)
    }

    /** Relaciona conceptos con reloj de 45 s, terminada después de [durationS] segundos. */
    private fun timed(durationS: Int, pausedSeconds: Int = 0, usedExtraTime: Boolean = false) = FinishedAttempt(
        clientAttemptId = "nuevo", content = content.copy(timeLimitSeconds = 45), answers = perfect, timedOut = false,
        startedAtMs = startedAt, finishedAtMs = startedAt + durationS * 1000L, tzOffsetMinutes = -360,
        pausedSeconds = pausedSeconds, usedExtraTime = usedExtraTime
    )

    @Test
    fun `con saldo el +30 s cobra 500 y alarga el reloj`() = runTest {
        offline()
        local.saveProgress(userId, officialProgress().copy(totalPoints = 600))

        val outcome = recorder().record(timed(durationS = 80, usedExtraTime = true)) as LessonOutcome.Pending

        assertEquals("completed", outcome.result.status)
        assertEquals(500, outcome.result.seedsSpent)
        val saved = local.pendingNow(userId).single()
        assertTrue(saved.usedExtraTime)
        assertEquals(0, saved.pausedSeconds)
    }

    @Test
    fun `sin saldo el +30 s no cuenta y se pierde por tiempo`() = runTest {
        offline()
        local.saveProgress(userId, officialProgress().copy(totalPoints = 300))

        val outcome = recorder().record(timed(durationS = 80, usedExtraTime = true)) as LessonOutcome.Pending

        assertEquals("failed", outcome.result.status)
        assertEquals(0, outcome.result.seedsSpent)
    }

    @Test
    fun `la pausa no cuenta como tiempo jugado`() = runTest {
        offline()

        val outcome = recorder().record(timed(durationS = 200, pausedSeconds = 170)) as LessonOutcome.Pending

        assertEquals("completed", outcome.result.status)
        assertEquals(170, local.pendingNow(userId).single().pausedSeconds)
    }

    @Test
    fun `si sube, lo gastado es lo que dice el servidor`() = runTest {
        local.saveProgress(userId, officialProgress().copy(totalPoints = 600))
        api.onSync = {
            syncOk(listOf(SyncAttemptResult(
                clientAttemptId = "nuevo", status = "accepted",
                attempt = SyncedAttempt(3, 100, 2, 0, 3, 90, false, "completed", seedsSpent = 500)
            )))
        }

        val outcome = recorder().record(timed(durationS = 80, usedExtraTime = true)) as LessonOutcome.Synced

        assertEquals(500, outcome.result.seedsSpent)
    }

    @Test
    fun `si no se puede guardar en el telefono no se sube nada`() = runTest {
        val failing = object : LocalStore by local {
            override suspend fun addPending(attempt: PendingAttempt) = throw IllegalStateException("disco lleno")
        }

        assertEquals(LessonOutcome.SaveFailed, recorder(store = failing).record(finished()))
        assertTrue(api.syncRequests.isEmpty())
    }

    @Test
    fun `sin sesion no se guarda`() = runTest {
        assertEquals(LessonOutcome.SaveFailed, recorder(user = null).record(finished()))
        assertTrue(local.pendingNow(userId).isEmpty())
    }
}
