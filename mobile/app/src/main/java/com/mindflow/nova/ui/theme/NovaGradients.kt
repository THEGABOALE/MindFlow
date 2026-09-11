package com.mindflow.nova.ui.theme

import androidx.compose.ui.graphics.Brush

/**
 * Degradé de las superficies "hero" de la app (banner de bienvenida, barra de
 * progreso, chip de recompensa): mismos dos colores de marca que ya usa el
 * login (índigo + morado), no un tono nuevo — así el degradé se siente parte
 * de la identidad visual en vez de un agregado suelto.
 */
val NovaHeroGradient = Brush.linearGradient(
    colors = listOf(NovaBlue, NovaPurple)
)
