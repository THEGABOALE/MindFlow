package com.mindflow.nova.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncStatusTextTest {

    @Test
    fun `sin conexion lo dice aunque haya pendientes`() {
        assertEquals("Sin conexión · tus resultados se subirán solos", syncStatusText(offline = true, pendingCount = 0))
        assertEquals("Sin conexión · tus resultados se subirán solos", syncStatusText(offline = true, pendingCount = 4))
    }

    @Test
    fun `con conexion cuenta lo que falta subir`() {
        assertEquals("Tienes 1 resultado por subir", syncStatusText(offline = false, pendingCount = 1))
        assertEquals("Tienes 3 resultados por subir", syncStatusText(offline = false, pendingCount = 3))
    }

    @Test
    fun `con conexion y nada pendiente no hay franja`() {
        assertNull(syncStatusText(offline = false, pendingCount = 0))
    }
}
