package com.mindflow.nova.ui.screens.home

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.model.StudentLevelProgress
import com.mindflow.nova.data.model.StudentProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class LevelProgressTest {

    private fun mission(id: Int, published: Boolean = true) = MissionResponse(
        id = id, title = "M$id", description = null, topic = null, orderIndex = id, pointsReward = 100,
        mechanic = "multiple_choice", timeLimitSeconds = null, maxPlumas = 3, isPublished = published
    )

    private val level = LevelResponse(
        id = 1, name = "Primaria alta", code = "primaria_alta", description = null, orderIndex = 1,
        missions = listOf(mission(1), mission(2), mission(3), mission(4, published = false))
    )

    private fun progress(completed: List<Int>, serverPercentage: Double) = StudentProgress(
        id = 7, fullName = "Ana", totalPoints = 0, missionsCompleted = completed.size,
        completedMissionIds = completed,
        levels = listOf(StudentLevelProgress(1, "Primaria alta", "primaria_alta", 1, serverPercentage, "in_progress"))
    )

    @Test
    fun `una mision completada sin subir ya cuenta en el porcentaje`() {
        // El servidor solo sabe de la 1 (33%); la 2 se jugó sin conexión.
        assertEquals(200.0 / 3, levelProgressPercentage(level, progress(listOf(1, 2), 100.0 / 3)), 0.001)
    }

    @Test
    fun `las misiones sin publicar no cuentan`() {
        assertEquals(100.0, levelProgressPercentage(level, progress(listOf(1, 2, 3), 0.0)), 0.001)
    }

    @Test
    fun `si el servidor dice mas, manda el servidor`() {
        assertEquals(100.0, levelProgressPercentage(level, progress(listOf(1), 100.0)), 0.001)
    }

    @Test
    fun `sin progreso es cero`() {
        assertEquals(0.0, levelProgressPercentage(level, null), 0.001)
    }
}
