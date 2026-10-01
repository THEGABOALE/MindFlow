package com.mindflow.nova.ui.screens.home

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.SessionGroup
import com.mindflow.nova.data.model.SessionUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LevelForStudentTest {

    private fun level(id: Int) = LevelResponse(
        id = id, name = "Nivel $id", code = "n$id", description = null, orderIndex = id, missions = emptyList()
    )

    private val levels = listOf(level(1), level(2), level(3))

    private fun student(levelId: Int?) = SessionUser(
        id = 2, fullName = "Gabriela", email = null, loginId = "garciaga", role = "student", centerId = 1,
        group = levelId?.let {
            SessionGroup(id = 1, name = "Sala", grade = "8vo", section = "A", schoolYear = 2026, levelId = it)
        }
    )

    @Test
    fun `un estudiante de secundaria ve su nivel y no el primero`() {
        assertEquals(3, levelForStudent(levels, student(levelId = 3))?.id)
    }

    @Test
    fun `sin sala no se le muestra ningun nivel`() {
        assertNull(levelForStudent(levels, student(levelId = null)))
    }

    @Test
    fun `si su nivel no vino en la lista no se le muestra otro`() {
        assertNull(levelForStudent(levels, student(levelId = 9)))
    }

    @Test
    fun `sin niveles no hay nada que mostrar`() {
        assertNull(levelForStudent(emptyList(), student(levelId = 1)))
    }
}
