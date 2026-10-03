package com.mindflow.nova.ui.components.zafiro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZafiroLinesTest {

    @Test
    fun `al acertar felicita y despues explica`() {
        assertEquals("¡Muy bien! Ese es el enfoque.", ZafiroLines.feedback(isCorrect = true, explanation = "Ese es el enfoque."))
    }

    @Test
    fun `al fallar anima y despues explica`() {
        assertEquals("Casi. Mira por qué: La dignidad es de todas.", ZafiroLines.feedback(isCorrect = false, explanation = "La dignidad es de todas."))
    }

    @Test
    fun `sin explicacion queda solo la frase`() {
        assertEquals("¡Muy bien!", ZafiroLines.feedback(isCorrect = true, explanation = ""))
        assertEquals("Casi.", ZafiroLines.feedback(isCorrect = false, explanation = "  "))
    }

    @Test
    fun `todas las frases tienen texto y van en tuteo`() {
        val voseo = listOf("querés", "podés", "tenés", "sabés", "mirá", "intentá", "seguí")

        ZafiroLines.all.forEach { line ->
            assertTrue("frase vacía", line.isNotBlank())
            voseo.forEach { word -> assertFalse("\"$line\" usa voseo", line.lowercase().contains(word)) }
        }
        assertEquals(12, ZafiroLines.all.size)
    }
}
