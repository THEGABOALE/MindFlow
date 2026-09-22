package com.mindflow.nova.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCelebrationTest {

    @Test
    fun `antes de que empiece la fase el avance es 0 y despues de que termina es 1`() {
        assertEquals(0f, phaseProgress(time = 0.5f, start = 1f, end = 2f), 0.0001f)
        assertEquals(1f, phaseProgress(time = 3f, start = 1f, end = 2f), 0.0001f)
    }

    @Test
    fun `dentro de la fase avanza de forma lineal`() {
        assertEquals(0.5f, phaseProgress(time = 1.5f, start = 1f, end = 2f), 0.0001f)
        assertEquals(0.25f, phaseProgress(time = 1.25f, start = 1f, end = 2f), 0.0001f)
    }
}
