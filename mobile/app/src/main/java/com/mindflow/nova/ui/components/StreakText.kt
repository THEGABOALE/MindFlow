package com.mindflow.nova.ui.components

import com.mindflow.nova.data.model.StudentStreak

/**
 * Textos de la racha: [caption] va debajo del número en el marcador; [title] y
 * [body] son los del diálogo que se abre al tocarlo.
 */
data class StreakMessage(val caption: String, val title: String, val body: String)

fun streakMessage(streak: StudentStreak): StreakMessage {
    val days = streak.days

    return when {
        streak.isActive && days <= 1 -> StreakMessage(
            caption = "día de racha",
            title = "¡Encendiste tu racha!",
            body = "Ya hiciste una misión hoy. Vuelve mañana para llegar a 2 días."
        )

        streak.isActive -> StreakMessage(
            caption = "días de racha",
            title = "¡Llevas $days días de racha!",
            body = "Ya hiciste una misión hoy. Vuelve mañana para llegar a ${days + 1} días."
        )

        days > 0 -> StreakMessage(
            caption = "racha congelada",
            title = "Tu racha está congelada",
            body = "Haz una misión hoy para descongelarla y llegar a ${days + 1} días."
        )

        else -> StreakMessage(
            caption = "racha congelada",
            title = "Todavía no tienes racha",
            body = "Haz una misión hoy para encenderla."
        )
    }
}
