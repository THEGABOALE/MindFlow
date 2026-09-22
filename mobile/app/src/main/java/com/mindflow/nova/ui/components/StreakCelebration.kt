package com.mindflow.nova.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.mindflow.nova.data.model.StudentStreak
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// Guion de la celebración, en segundos desde que aparece la pantalla:
//   0.0 - 0.9  el copo aparece congelado (con su onda de escarcha)
//   0.9 - 1.6  el hielo tiembla y se derrite en gotas
//   1.5 - 2.4  llamarada: un destello se expande por toda la pantalla y el fondo pasa de frío a fuego
//   1.6 en adelante  la llama toma el lugar del hielo y el contador sube un día
//   2.7  aparece el botón para continuar
private const val ICE_HOLD = 0.9f
private const val MELT_END = 1.6f
private const val FLARE_START = 1.5f
private const val FLARE_END = 2.4f
private const val FLAME_AT = 1.6f
private const val BUTTON_AT = 2.7f
private const val TOTAL_SECONDS = 3f

private val IceTop = Color(0xFF071726)
private val IceBottom = Color(0xFF1B5E85)
private val FireTop = Color(0xFF1C0803)
private val FireBottom = Color(0xFFB23A00)
private val FlareCore = Color(0xFFFFF3B0)
private val FlareMid = Color(0xFFFFC107)
private val FlareOuter = Color(0xFFFF6D00)
private val DropColor = Color(0xFF81D4FA)
private val ColdText = Color(0xFFBFE6FF)

/** Avance 0..1 de una fase que empieza en [start] y termina en [end] (segundos). */
internal fun phaseProgress(time: Float, start: Float, end: Float): Float =
    ((time - start) / (end - start)).coerceIn(0f, 1f)

private fun easeOut(t: Float): Float = 1f - (1f - t) * (1f - t)

private fun easeInOut(t: Float): Float = t * t * (3f - 2f * t)

/**
 * Momento de racha a pantalla completa: la racha estaba congelada y este
 * intento la enciende. El copo de hielo tiembla y se derrite, una llamarada
 * recorre toda la pantalla y en su lugar queda una llama con el contador ya
 * sumado. [days] son los días con hoy incluido.
 */
@Composable
fun StreakCelebrationScreen(
    days: Int,
    onContinue: () -> Unit
) {
    val time = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        time.animateTo(TOTAL_SECONDS, tween((TOTAL_SECONDS * 1000).toInt(), easing = LinearEasing))
    }

    val flameVisible by remember { derivedStateOf { time.value >= FLAME_AT } }
    val iceVisible by remember { derivedStateOf { time.value < FLARE_END } }
    val buttonVisible by remember { derivedStateOf { time.value >= BUTTON_AT } }

    val embers = rememberInfiniteTransition(label = "embers")
    val emberPhase by embers.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "emberPhase"
    )

    // Sobre este fondo oscuro los iconos de la barra de estado tienen que ser claros.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false

        onDispose {
            if (previous != null) controller.isAppearanceLightStatusBars = previous
        }
    }

    var iconCenter by remember { mutableStateOf(Offset.Zero) }
    val message = streakMessage(StudentStreak(days = days, isActive = true, lastActivityDate = null))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawCelebration(time.value, emberPhase, iconCenter) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .onGloballyPositioned { iconCenter = it.boundsInRoot().center }
                    .graphicsLayer {
                        val t = time.value
                        val melt = phaseProgress(t, ICE_HOLD, MELT_END)
                        val vanish = phaseProgress(t, MELT_END - 0.3f, FLARE_START + 0.35f)

                        // El copo solo se ve (y tiembla) mientras es hielo; la llama va en otra capa.
                        if (t < FLAME_AT + 0.4f) {
                            rotationZ = sin(t * 60f) * 5f * sin(PI.toFloat() * melt)
                            val s = 1f + 0.12f * melt - 0.5f * vanish
                            scaleX = s
                            scaleY = s
                            translationY = melt * 14.dp.toPx()
                            alpha = 1f - vanish
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (iceVisible) {
                    StreakIce(iconSize = 130.dp)
                }
            }

            AnimatedContent(
                targetState = flameVisible,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "days"
            ) { warm ->
                Text(
                    text = (if (warm) days else (days - 1).coerceAtLeast(0)).toString(),
                    color = if (warm) Color.White else ColdText,
                    fontSize = 72.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            AnimatedContent(
                targetState = flameVisible,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(250)) },
                label = "message"
            ) { warm ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (warm) message.title else "Tu racha estaba congelada",
                        color = if (warm) Color.White else ColdText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (warm) message.body else "Completaste una misión hoy…",
                        color = (if (warm) Color.White else ColdText).copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // La llama se dibuja encima del hielo, centrada en el mismo punto, para que se lea como la transformación.
        if (flameVisible) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = iconCenter.x - size.width / 2f
                        translationY = iconCenter.y - size.height / 2f
                    },
                contentAlignment = Alignment.Center
            ) {
                StreakFlame(iconSize = 130.dp, ignitionId = 1)
            }
        }

        Button(
            onClick = onContinue,
            enabled = buttonVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .fillMaxWidth()
                .graphicsLayer { alpha = phaseProgress(time.value, BUTTON_AT, BUTTON_AT + 0.3f) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF7A2200),
                disabledContainerColor = Color.White,
                disabledContentColor = Color(0xFF7A2200)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = "Continuar", fontWeight = FontWeight.Bold)
        }
    }
}

private fun DrawScope.drawCelebration(time: Float, emberPhase: Float, iconCenter: Offset) {
    val flare = phaseProgress(time, FLARE_START, FLARE_END)
    val warm = easeInOut(flare)

    val center = if (iconCenter == Offset.Zero) Offset(size.width / 2f, size.height * 0.36f) else iconCenter
    val unit = size.minDimension

    // El fondo de fuego no se mezcla con el de hielo: lo va reemplazando desde el
    // centro hacia afuera (mezclar azul con naranja da un gris verdoso feo).
    val reach = hypot(size.width, size.height) * 1.1f * easeOut(flare)

    drawRect(Brush.verticalGradient(listOf(IceTop, IceBottom)))
    if (flare > 0.01f) {
        drawCircle(
            brush = Brush.verticalGradient(listOf(FireTop, FireBottom)),
            radius = reach,
            center = center
        )
    }

    // Gotas del hielo derritiéndose.
    val melt = phaseProgress(time, ICE_HOLD, MELT_END + 0.4f)
    if (melt > 0f && melt < 1f) {
        for (i in 0 until 7) {
            val offsetX = (i - 3) * unit * 0.045f
            val delay = i * 0.06f
            val fall = ((melt - delay) / (1f - delay)).coerceIn(0f, 1f)

            drawCircle(
                color = DropColor.copy(alpha = (1f - fall) * 0.85f),
                radius = unit * (0.010f + 0.004f * (i % 3)),
                center = Offset(center.x + offsetX, center.y + unit * 0.05f + fall * unit * 0.22f)
            )
        }
    }

    // Llamarada: un frente brillante que se expande desde el hielo hasta cubrir toda la pantalla.
    if (flare > 0.01f && flare < 1f) {
        val fade = 1f - flare

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    0.70f to FlareOuter.copy(alpha = 0.25f * fade),
                    0.92f to FlareMid.copy(alpha = 0.90f * fade),
                    1f to Color.Transparent
                ),
                center = center,
                radius = reach
            ),
            radius = reach,
            center = center
        )

        // Núcleo incandescente en el punto donde estaba el hielo.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(FlareCore.copy(alpha = 0.95f * fade), Color.Transparent),
                center = center,
                radius = unit * 0.6f
            ),
            radius = unit * 0.6f,
            center = center
        )

        drawRect(Color.White.copy(alpha = 0.30f * fade * fade))

        for (i in 0 until 28) {
            val angle = i * (2.0 * PI / 28) + (i % 3) * 0.11
            val distance = reach * (0.35f + 0.65f * ((i * 37) % 10) / 10f)

            drawCircle(
                color = (if (i % 2 == 0) FlareMid else FlareOuter).copy(alpha = fade),
                radius = unit * (0.006f + 0.004f * (i % 4)) * (1f - 0.5f * flare),
                center = Offset(
                    x = center.x + (cos(angle) * distance).toFloat(),
                    y = center.y + (sin(angle) * distance).toFloat()
                )
            )
        }
    }

    // Brasas que suben por toda la pantalla una vez encendido el fuego.
    if (warm > 0.3f) {
        for (i in 0 until 16) {
            val seed = i * 0.618034f
            val rise = (emberPhase + seed) % 1f
            val x = ((seed * 7.3f) % 1f) * size.width + sin(rise * 12.5f + i) * size.width * 0.03f
            val y = size.height * (1.05f - rise * 1.2f)

            drawCircle(
                color = (if (i % 3 == 0) FlareMid else FlareOuter).copy(
                    alpha = sin(PI * rise).toFloat() * 0.8f * warm
                ),
                radius = unit * 0.005f * (1 + i % 4),
                center = Offset(x, y)
            )
        }
    }
}
