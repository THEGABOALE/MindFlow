package com.mindflow.nova.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los pares de colores que se usan como texto o como borde de un elemento
 * tienen que cumplir WCAG 2.1 AA. Si alguien cambia un color de la paleta y
 * baja del mínimo, este test lo dice.
 */
class ContrastTest {

    private val text = 4.5
    private val graphic = 3.0

    private fun assertContrast(name: String, foreground: Color, background: Color, minimum: Double) {
        val ratio = contrastRatio(foreground, background)
        assertTrue("$name: %.2f:1, mínimo %.1f:1".format(ratio, minimum), ratio >= minimum)
    }

    @Test
    fun `la formula da los extremos conocidos`() {
        assertEquals(21.0, contrastRatio(Color.White, Color.Black), 0.01)
        assertEquals(1.0, contrastRatio(Color.White, Color.White), 0.01)
    }

    @Test
    fun `semillas`() {
        assertContrast("semillas sobre crema", LightNovaPalette.gold, LightNovaPalette.goldLight, text)
        assertContrast("semillas sobre blanco", LightNovaPalette.gold, Color.White, text)
        assertContrast("semillas en oscuro", DarkNovaPalette.gold, DarkNovaPalette.goldLight, text)
    }

    @Test
    fun `botones morados`() {
        assertContrast("botón claro", LightNovaPalette.onPurple, LightNovaPalette.purple, text)
        assertContrast("botón oscuro", DarkNovaPalette.onPurple, DarkNovaPalette.purple, text)
    }

    @Test
    fun `acierto y error`() {
        for (palette in listOf(LightNovaPalette, DarkNovaPalette)) {
            val mode = if (palette.isDark) "oscuro" else "claro"
            assertContrast("acierto como texto ($mode)", palette.success, palette.successBackground, text)
            assertContrast("error como texto ($mode)", palette.error, palette.errorBackground, text)
            assertContrast("botón de acierto ($mode)", palette.onText, palette.success, text)
            assertContrast("botón de error ($mode)", palette.onText, palette.error, text)
        }
    }

    @Test
    fun `pie del login`() {
        assertContrast("pie del login", NovaLoginFooterText, NovaLoginCard, text)
    }

    @Test
    fun `barra de progreso vacia sobre la tarjeta lila`() {
        assertContrast("borde de la barra (claro)", LightNovaPalette.trackBorder, LightNovaPalette.lightPurple, graphic)
        assertContrast("borde de la barra (oscuro)", DarkNovaPalette.trackBorder, DarkNovaPalette.lightPurple, graphic)
    }
}
