package com.mindflow.nova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun NOVATheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkNovaPalette else LightNovaPalette

    // Los componentes de Material que no reciben color explícito (texto por
    // defecto, ripples, contentColorFor de las Surface) toman el de la paleta.
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.purple,
            onPrimary = Color.White,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            surfaceTint = palette.surface
        )
    } else {
        lightColorScheme(
            primary = palette.purple,
            onPrimary = Color.White,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border,
            surfaceTint = palette.surface
        )
    }

    CompositionLocalProvider(LocalNovaPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
