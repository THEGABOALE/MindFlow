package com.mindflow.nova.ui.components

import com.mindflow.nova.data.model.StudentStreak
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakTextTest {

    private fun streak(days: Int, isActive: Boolean) =
        StudentStreak(days = days, isActive = isActive, lastActivityDate = null)

    @Test
    fun `racha activa de un dia usa singular y anuncia que se encendio`() {
        val message = streakMessage(streak(days = 1, isActive = true))

        assertEquals("día de racha", message.caption)
        assertEquals("¡Encendiste tu racha!", message.title)
        assertEquals("Ya hiciste una misión hoy. Vuelve mañana para llegar a 2 días.", message.body)
    }

    @Test
    fun `racha activa de varios dias dice cuantos lleva y a cuanto llega mañana`() {
        val message = streakMessage(streak(days = 3, isActive = true))

        assertEquals("días de racha", message.caption)
        assertEquals("¡Llevas 3 días de racha!", message.title)
        assertEquals("Ya hiciste una misión hoy. Vuelve mañana para llegar a 4 días.", message.body)
    }

    @Test
    fun `racha congelada con dias explica como descongelarla`() {
        val message = streakMessage(streak(days = 2, isActive = false))

        assertEquals("racha congelada", message.caption)
        assertEquals("Tu racha está congelada", message.title)
        assertEquals("Haz una misión hoy para descongelarla y llegar a 3 días.", message.body)
    }

    @Test
    fun `sin racha invita a encenderla`() {
        val message = streakMessage(streak(days = 0, isActive = false))

        assertEquals("racha congelada", message.caption)
        assertEquals("Todavía no tienes racha", message.title)
        assertEquals("Haz una misión hoy para encenderla.", message.body)
    }
}
