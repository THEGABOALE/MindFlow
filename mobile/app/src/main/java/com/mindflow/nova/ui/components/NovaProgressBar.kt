package com.mindflow.nova.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mindflow.nova.ui.theme.NovaHeroGradient

@Composable
fun NovaProgressBar(progress: Float) {
    // Se anima en vez de saltar directo al valor final, para que se sienta
    // como que el avance "llena" la barra en vez de aparecer ya lleno.
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "progressFill"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFEDE6EF))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50.dp))
                .background(NovaHeroGradient)
        )
    }
}
