package com.mindflow.nova.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Con la fuente a partir de 1,5× las dos tarjetas no entran lado a lado sin partir palabras. */
internal fun stackStats(fontScale: Float): Boolean = fontScale >= 1.5f

/**
 * Dos tarjetas de cifras (misiones, semillas): lado a lado con la fuente
 * normal y una debajo de la otra con la fuente grande, para que una palabra
 * como "completadas" no quede partida.
 */
@Composable
fun StatPair(
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    if (stackStats(LocalDensity.current.fontScale)) {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            first(Modifier.fillMaxWidth())
            second(Modifier.fillMaxWidth())
        }
    } else {
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            first(Modifier.weight(1f))
            second(Modifier.weight(1f))
        }
    }
}
