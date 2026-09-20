package com.mindflow.nova.ui.screens.lessons

import com.mindflow.nova.data.model.MissionResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class MissionInfoTest {

    private fun mission(id: Int) = MissionResponse(
        id = id,
        title = "Misión $id",
        description = null,
        topic = null,
        orderIndex = id,
        pointsReward = 50,
        mechanic = "multiple_choice",
        timeLimitSeconds = null,
        maxPlumas = 3,
        isPublished = true
    )

    private val missions = listOf(mission(1), mission(2), mission(3))

    @Test
    fun `sin progreso solo la primera misión está desbloqueada y es la actual`() {
        val states = computeMissionStates(missions, emptySet())

        assertEquals(listOf(true, false, false), states.map { it.isUnlocked })
        assertEquals(0, currentMissionIndex(states))
    }

    @Test
    fun `completar una misión desbloquea la siguiente y la vuelve la actual`() {
        val states = computeMissionStates(missions, setOf(1))

        assertEquals(listOf(true, false, false), states.map { it.isCompleted })
        assertEquals(listOf(true, true, false), states.map { it.isUnlocked })
        assertEquals(1, currentMissionIndex(states))
    }

    @Test
    fun `saltarse una misión no desbloquea las de después`() {
        val states = computeMissionStates(missions, setOf(1, 3))

        assertEquals(listOf(true, false, true), states.map { it.isCompleted })
        assertEquals(1, currentMissionIndex(states))
    }

    @Test
    fun `con todas completadas ya no hay misión actual`() {
        val states = computeMissionStates(missions, setOf(1, 2, 3))

        assertEquals(-1, currentMissionIndex(states))
    }

    @Test
    fun `mechanicLabel traduce cada minijuego y cae a un genérico si no lo conoce`() {
        assertEquals("Opción múltiple", mechanicLabel("multiple_choice"))
        assertEquals("Relaciona conceptos", mechanicLabel("matching"))
        assertEquals("Verdadero o falso", mechanicLabel("true_false"))
        assertEquals("Sopa de letras", mechanicLabel("word_search"))
        assertEquals("Minijuego", mechanicLabel(null))
        assertEquals("Minijuego", mechanicLabel("otro"))
    }
}
