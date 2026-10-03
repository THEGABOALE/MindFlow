package com.mindflow.nova.data.offline

import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.model.StudentStreak

/**
 * El progreso que se muestra: el último que mandó el servidor más lo jugado en
 * el teléfono que todavía no se subió. Es provisional; al sincronizar llega el
 * oficial y los pendientes desaparecen.
 *
 * - Las misiones completadas pendientes (sin contar repasos) se suman una vez.
 * - Las semillas provisionales se suman todas, menos lo gastado en potenciadores.
 * - La racha se recalcula con [today]: lo último guardado puede ser de otro
 *   día, y lo jugado sin conexión cuenta para el día en que se jugó.
 *
 * @param today fecha local de hoy, "YYYY-MM-DD"
 */
fun mergePending(official: StudentProgress?, pending: List<PendingAttempt>, today: String): StudentProgress? {
    if (official == null) return null

    val newlyCompleted = pending
        .filter { it.provisional.status == "completed" && !it.provisional.isReview }
        .map { it.missionId }
        .distinct()
        .filterNot { it in official.completedMissionIds }

    return official.copy(
        totalPoints = official.totalPoints + pending.sumOf { it.provisional.pointsEarned - it.provisional.seedsSpent },
        missionsCompleted = official.missionsCompleted + newlyCompleted.size,
        completedMissionIds = official.completedMissionIds + newlyCompleted,
        // finishedAt va con el huso del dispositivo: sus primeros 10 caracteres son el día local.
        streak = official.streak?.let { streak -> recalculateStreak(streak, pending.map { it.finishedAt.take(10) }, today) }
    )
}

/**
 * Misma regla que backend/src/services/streak.service.js: activa si jugó hoy,
 * congelada si lo último fue ayer y perdida (0 días) si fue antes.
 *
 * Del servidor solo se sabe la racha que termina en su lastActivityDate; si
 * venía perdida (0 días) se cuenta ese último día como uno solo.
 */
private fun recalculateStreak(official: StudentStreak, pendingDays: List<String>, today: String): StudentStreak {
    val todayNumber = dayNumber(today)
    val days = mutableSetOf<Long>()

    official.lastActivityDate?.let { last ->
        val lastNumber = dayNumber(last)
        repeat(maxOf(official.days, 1)) { days += lastNumber - it }
    }
    // Los días futuros solo pueden venir de un reloj mal puesto: no cuentan.
    pendingDays.mapTo(days) { dayNumber(it) }
    days.removeAll { it > todayNumber }

    val last = days.maxOrNull() ?: return StudentStreak(days = 0, isActive = false, lastActivityDate = null)
    val gap = todayNumber - last
    val lastActivityDate = dayFromNumber(last)

    if (gap >= 2) return StudentStreak(days = 0, isActive = false, lastActivityDate = lastActivityDate)

    var run = 0
    while ((last - run) in days) run++

    return StudentStreak(days = run, isActive = gap == 0L, lastActivityDate = lastActivityDate)
}
