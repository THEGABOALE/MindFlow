package com.mindflow.nova.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colores de marca fijos: no cambian entre tema claro y oscuro (degradé hero, botones sobre morado). */
internal val BrandBlue = Color(0xFF1600B5)
internal val BrandPurple = Color(0xFF82368C)

/**
 * Todos los colores de la app que dependen del tema. Los tokens de
 * NovaColors.kt (NovaText, NovaBackground, ...) leen de la paleta activa, así
 * que las pantallas no saben si están en claro u oscuro.
 */
@Immutable
class NovaPalette(
    val isDark: Boolean,
    val purple: Color,
    val blue: Color,
    val lightPurple: Color,
    val softPurple: Color,
    val text: Color,
    val onText: Color,
    val textSecondary: Color,
    val background: Color,
    val surface: Color,
    val border: Color,
    val track: Color,
    val dark: Color,
    val locked: Color,
    val gold: Color,
    val goldLight: Color,
    val success: Color,
    val successBackground: Color,
    val error: Color,
    val errorBackground: Color,
    val neutralCard: Color,
    val infoBackground: Color,
    val choiceIdle: Color
)

val LightNovaPalette = NovaPalette(
    isDark = false,
    purple = BrandPurple,
    blue = BrandBlue,
    lightPurple = Color(0xFFF5ECF7),
    softPurple = Color(0xFFE9D8EE),
    text = Color(0xFF211A24),
    onText = Color.White,
    textSecondary = Color(0xFF6F6473),
    background = Color(0xFFFCF8FD),
    surface = Color.White,
    border = Color(0xFFE7D8EA),
    track = Color(0xFFEDE6EF),
    dark = Color(0xFF211A2E),
    locked = Color(0xFFD9D2DC),
    gold = Color(0xFFB8860B),
    goldLight = Color(0xFFFBF0DC),
    success = Color(0xFF2E9E5B),
    successBackground = Color(0xFFE6F7EA),
    error = Color(0xFFC0392B),
    errorBackground = Color(0xFFFBE7E5),
    neutralCard = Color(0xFFF0EDF2),
    infoBackground = Color(0xFFEDEAFF),
    choiceIdle = Color(0xFFDCEBFB)
)

// Morado y azul más claros que los de marca: los de marca casi no se distinguen
// del fondo oscuro cuando se usan como texto o icono.
val DarkNovaPalette = NovaPalette(
    isDark = true,
    purple = Color(0xFFBE6ACB),
    blue = Color(0xFF9C8FFF),
    lightPurple = Color(0xFF2B2233),
    softPurple = Color(0xFF3A2D44),
    text = Color(0xFFF2ECF5),
    onText = Color(0xFF15111A),
    textSecondary = Color(0xFFB5A9BB),
    background = Color(0xFF15111A),
    surface = Color(0xFF1F1A26),
    border = Color(0xFF3A3142),
    track = Color(0xFF3A3142),
    dark = Color(0xFF4B4166),
    locked = Color(0xFF3A3440),
    gold = Color(0xFFE0A93B),
    goldLight = Color(0xFF3B2F14),
    success = Color(0xFF4CC985),
    successBackground = Color(0xFF173B27),
    error = Color(0xFFFF7F72),
    errorBackground = Color(0xFF40201D),
    neutralCard = Color(0xFF2A252E),
    infoBackground = Color(0xFF2B2850),
    choiceIdle = Color(0xFF263547)
)

val LocalNovaPalette = staticCompositionLocalOf { LightNovaPalette }
