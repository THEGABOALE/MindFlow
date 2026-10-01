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

        // Sin días no hay nada congelado: decirle "congelada" a quien recién
        // empieza suena a que ya hizo algo mal.
        else -> StreakMessage(
            caption = "empieza tu racha",
            title = "Todavía no tienes racha",
            body = "Haz una misión hoy para encenderla."
        )
    }
}

/**
 * Título de la primera parte del momento de racha, antes de que aparezca la
 * llama. [daysWithToday] son los días con hoy incluido: si es 1, antes no
 * había racha (es la primera, o se había perdido), así que no estaba
 * "congelada".
 */
fun streakColdTitle(daysWithToday: Int): String =
    if (daysWithToday > 1) "Tu racha estaba congelada" else "Todavía no tenías racha"
