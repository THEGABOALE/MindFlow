package com.mindflow.nova.ui.screens.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileInitialsTest {

    @Test
    fun `toma la inicial de las dos primeras palabras`() {
        assertEquals("GG", initialsOf("Gabriela García"))
        assertEquals("GM", initialsOf("Gabriela María García López"))
    }

    @Test
    fun `una sola palabra da una sola inicial y las minúsculas se suben`() {
        assertEquals("E", initialsOf("estudiante"))
    }

    @Test
    fun `ignora espacios sobrantes y nombre vacío`() {
        assertEquals("GG", initialsOf("  Gabriela   García "))
        assertEquals("", initialsOf(""))
    }
}
