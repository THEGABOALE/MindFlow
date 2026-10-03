package com.mindflow.nova.ui.screens.lessons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtraTimeTest {

    @Test
    fun `con saldo el boton dice el precio`() {
        assertEquals("+30 s · 500 semillas", extraTimeLabel(800))
        assertEquals("+30 s · 500 semillas", extraTimeLabel(500))
    }

    @Test
    fun `sin saldo dice cuantas faltan`() {
        assertEquals("Te faltan 380 semillas para +30 s", extraTimeLabel(120))
        assertEquals("Te faltan 500 semillas para +30 s", extraTimeLabel(0))
    }

    @Test
    fun `el boton aparece cuando quedan 15 segundos o menos y no se compro`() {
        assertTrue(showExtraTime(secondsLeft = 15, alreadyUsed = false, hasTimeLimit = true))
        assertTrue(showExtraTime(secondsLeft = 1, alreadyUsed = false, hasTimeLimit = true))
        assertFalse(showExtraTime(secondsLeft = 16, alreadyUsed = false, hasTimeLimit = true))
        assertFalse(showExtraTime(secondsLeft = 10, alreadyUsed = true, hasTimeLimit = true))
        assertFalse(showExtraTime(secondsLeft = 0, alreadyUsed = false, hasTimeLimit = true))
    }

    @Test
    fun `sin limite de tiempo de la mision no se ofrece el boton`() {
        // El reloj de respaldo no lo valida el servidor: comprar ahí cobraría
        // en pantalla algo que nunca se cobra ni se respeta.
        assertFalse(showExtraTime(secondsLeft = 10, alreadyUsed = false, hasTimeLimit = false))
    }

    @Test
    fun `la nota del cierre aparece solo si se gasto`() {
        assertEquals("Usaste +30 s: −500 semillas", spentNotice(500))
        assertNull(spentNotice(0))
    }
}
