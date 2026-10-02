package com.mindflow.nova.data.offline

import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.model.StudentStreak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressMergeTest {

    private val today = "2026-10-01"

    private fun official(
        points: Int = 100,
        completed: List<Int> = listOf(1),
        streak: StudentStreak? = StudentStreak(days = 3, isActive = false, lastActivityDate = "2026-09-30")
    ) = StudentProgress(
        id = 7,
        fullName = "Ana",
        totalPoints = points,
        missionsCompleted = completed.size,
        completedMissionIds = completed,
        streak = streak,
        levels = emptyList()
    )

    private var nextId = 0

    private fun pending(
        missionId: Int,
        day: String = today,
        status: String = "completed",
        points: Int = 50,
        isReview: Boolean = false
    ) = PendingAttempt(
        clientAttemptId = "id-${nextId++}",
        userId = 7,
        missionId = missionId,
        startedAt = "${day}T10:00:00.000-06:00",
        finishedAt = "${day}T10:05:00.000-06:00",
        tzOffsetMinutes = -360,
        timedOut = false,
        answers = emptyList(),
        provisional = AttemptResult(
            id = 0, missionId = missionId, score = 100, correctAnswers = 3, wrongAnswers = 0,
            plumasLeft = 3, pointsEarned = points, isReview = isReview, status = status
        )
    )

    @Test
    fun `sin progreso oficial no hay nada que mostrar`() {
        assertNull(mergePending(null, listOf(pending(2)), today))
    }

    @Test
    fun `sin pendientes queda igual`() {
        val progress = official(streak = StudentStreak(3, true, today))

        assertEquals(progress, mergePending(progress, emptyList(), today))
    }

    @Test
    fun `sin pendientes una racha activa de ayer hoy se ve congelada`() {
        // Lo último guardado fue ayer con la racha encendida; hoy no hay red.
        val merged = mergePending(official(streak = StudentStreak(3, true, "2026-09-30")), emptyList(), today)!!

        assertEquals(StudentStreak(3, false, "2026-09-30"), merged.streak)
    }

    @Test
    fun `las misiones completadas pendientes se suman sin duplicar`() {
        val merged = mergePending(
            official(completed = listOf(1)),
            listOf(pending(2), pending(2), pending(1)),
            today
        )!!

        assertEquals(listOf(1, 2), merged.completedMissionIds)
        assertEquals(2, merged.missionsCompleted)
    }

    @Test
    fun `los repasos y las misiones perdidas no cuentan como completadas`() {
        val merged = mergePending(
            official(completed = listOf(1)),
            listOf(pending(2, status = "failed", points = 0), pending(3, isReview = true, points = 25)),
            today
        )!!

        assertEquals(listOf(1), merged.completedMissionIds)
        assertEquals(1, merged.missionsCompleted)
        assertEquals(125, merged.totalPoints)
    }

    @Test
    fun `se suman las semillas provisionales`() {
        val merged = mergePending(official(points = 100), listOf(pending(2, points = 75), pending(3, points = 50)), today)!!

        assertEquals(225, merged.totalPoints)
    }

    @Test
    fun `jugar hoy enciende la racha congelada con un dia mas`() {
        val merged = mergePending(official(), listOf(pending(2)), today)!!

        assertEquals(StudentStreak(days = 4, isActive = true, lastActivityDate = today), merged.streak)
    }

    @Test
    fun `una mision perdida hoy tambien cuenta para la racha`() {
        val merged = mergePending(official(), listOf(pending(2, status = "failed", points = 0)), today)!!

        assertEquals(StudentStreak(4, true, today), merged.streak)
    }

    @Test
    fun `si la racha oficial ya esta activa hoy no cambia`() {
        val streak = StudentStreak(days = 5, isActive = true, lastActivityDate = today)
        val merged = mergePending(official(streak = streak), listOf(pending(2)), today)!!

        assertEquals(streak, merged.streak)
    }

    @Test
    fun `jugar hoy despues de perder la racha empieza en 1`() {
        val lost = StudentStreak(days = 0, isActive = false, lastActivityDate = "2026-09-25")
        val merged = mergePending(official(streak = lost), listOf(pending(2)), today)!!

        assertEquals(StudentStreak(1, true, today), merged.streak)
    }

    @Test
    fun `un pendiente de un dia que ya contaba no toca la racha`() {
        val streak = StudentStreak(days = 3, isActive = false, lastActivityDate = "2026-09-30")
        val merged = mergePending(official(streak = streak), listOf(pending(2, day = "2026-09-29")), today)!!

        assertEquals(streak, merged.streak)
    }

    @Test
    fun `un pendiente de ayer congela la racha en vez de perderla`() {
        // El servidor solo sabe del 28; el 30 se jugó sin conexión.
        val lost = StudentStreak(days = 0, isActive = false, lastActivityDate = "2026-09-28")
        val merged = mergePending(official(streak = lost), listOf(pending(2, day = "2026-09-30")), today)!!

        assertEquals(StudentStreak(1, false, "2026-09-30"), merged.streak)
    }

    @Test
    fun `una racha guardada de dias atras se recalcula con el dia de hoy`() {
        // Se guardó activa el 28 y no se volvió a conectar: hoy ya está perdida.
        val stale = StudentStreak(days = 6, isActive = true, lastActivityDate = "2026-09-28")
        val merged = mergePending(official(streak = stale), listOf(pending(2, day = "2026-09-29")), today)!!

        assertFalse(merged.streak!!.isActive)
        assertEquals(0, merged.streak!!.days)

        val playedToday = mergePending(official(streak = stale), listOf(pending(2)), today)!!
        assertTrue(playedToday.streak!!.isActive)
        assertEquals(1, playedToday.streak!!.days)
    }

    @Test
    fun `sin racha oficial no se inventa una`() {
        val merged = mergePending(official(streak = null), listOf(pending(2)), today)!!

        assertNull(merged.streak)
    }
}
