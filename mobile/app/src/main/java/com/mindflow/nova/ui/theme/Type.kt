package com.mindflow.nova.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Escala tipográfica de NOVA. Las pantallas usan estos estilos
 * (MaterialTheme.typography.titleMedium, ...) en vez de escribir el tamaño a
 * mano: así todo crece parejo cuando la persona agranda la fuente del
 * teléfono, y un cambio de tamaño se hace en un solo lugar. labelLarge (el de
 * los botones) queda con el valor de Material.
 */
val Typography = Typography(
    displaySmall = style(26, 32, FontWeight.Black),
    titleLarge = style(22, 28, FontWeight.Black),
    titleMedium = style(17, 22, FontWeight.Bold),
    // Es el estilo por defecto de todo Text sin tamaño: se queda como estaba.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = style(13, 18, FontWeight.Normal),
    labelSmall = style(12, 16, FontWeight.SemiBold)
)

private fun style(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)
