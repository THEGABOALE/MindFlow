package com.mindflow.nova.ui.components.zafiro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText

// Zafiro, la mascota que guía la narrativa de NOVA. Todavía no tiene arte:
// estos son los recuadros provisionales del wireframe, en un solo lugar.
// Cuando llegue el dibujo, se cambia acá según la pose y el resto de la app
// no se entera.

/** Cómo está Zafiro en cada momento; sirve para elegir el dibujo cuando exista. */
enum class ZafiroPose { SALUDA, EXPLICA, CELEBRA, TRISTE, PIENSA }

private const val ZAFIRO_NAME = "Zafiro"

/**
 * Zafiro chico, junto a una pregunta o en un cierre de misión. [compact] es
 * para los espacios donde no entra el nombre (muestra la inicial).
 */
@Composable
fun ZafiroBox(
    @Suppress("UNUSED_PARAMETER") pose: ZafiroPose,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = NovaLightPurple
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (compact) ZAFIRO_NAME.take(1) else ZAFIRO_NAME,
                color = NovaPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Zafiro grande del onboarding, con lo que dice en un globo. */
@Composable
fun ZafiroIllustration(
    @Suppress("UNUSED_PARAMETER") pose: ZafiroPose,
    line: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF626262), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = ZAFIRO_NAME, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)

            if (line != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color(0xFFD9D9D9), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = line,
                        color = NovaText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/** Zafiro redondo dentro del banner del Inicio. */
@Composable
fun ZafiroBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(86.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = ZAFIRO_NAME,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
