package com.mindflow.nova.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mindflow.nova.data.model.StudentStreak
import com.mindflow.nova.ui.theme.NovaFireAccent
import com.mindflow.nova.ui.theme.NovaFireBackground
import com.mindflow.nova.ui.theme.NovaIceAccent
import com.mindflow.nova.ui.theme.NovaIceBackground
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaSurface
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

private val FlameOuter = Color(0xFFFF6D00)
private val FlameInner = Color(0xFFFFC107)
private val IceMain = Color(0xFF39A9E8)
private val IceLight = Color(0xFFB3E5FC)

/** Chispa que sale disparada al encenderse la llama: ángulo desde la vertical, alcance y tamaño relativos al ícono. */
private class Spark(val angleDeg: Float, val reach: Float, val radius: Float, val color: Color)

private val Sparks = listOf(
    Spark(-58f, 1.00f, 0.075f, Color(0xFFFFB300)),
    Spark(-32f, 1.25f, 0.060f, Color(0xFFFF6D00)),
    Spark(-10f, 1.45f, 0.090f, Color(0xFFFFD54F)),
    Spark(12f, 1.15f, 0.060f, Color(0xFFFF8F00)),
    Spark(34f, 1.35f, 0.080f, Color(0xFFFFB300)),
    Spark(58f, 1.00f, 0.065f, Color(0xFFFF6D00)),
    Spark(0f, 1.60f, 0.055f, Color(0xFFFFE082))
)

/** Destello de escarcha que titila alrededor del copo: ángulo, distancia, tamaño y desfase en el ciclo. */
private class Twinkle(val angleDeg: Float, val distance: Float, val size: Float, val phase: Float)

private val Twinkles = listOf(
    Twinkle(-50f, 0.85f, 0.16f, 0.00f),
    Twinkle(40f, 0.95f, 0.12f, 0.35f),
    Twinkle(150f, 0.85f, 0.14f, 0.60f),
    Twinkle(-140f, 0.90f, 0.11f, 0.85f)
)

/**
 * Marcador de racha: llama cuando está activa (hizo una misión hoy), hielo
 * cuando está congelada. Al pasar de congelada a activa mientras está en
 * pantalla, la llama se enciende con un estallido de chispas.
 */
@Composable
fun StreakBadge(
    streak: StudentStreak,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val message = streakMessage(streak)
    val accent = if (streak.isActive) NovaFireAccent else NovaIceAccent
    val background = if (streak.isActive) NovaFireBackground else NovaIceBackground

    var ignitionId by remember { mutableIntStateOf(0) }
    var lastActive by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(streak.isActive) {
        if (lastActive == false && streak.isActive) {
            ignitionId++
        }
        lastActive = streak.isActive
    }

    // Sin Surface ni clip para que las chispas puedan salirse del marcador.
    Row(
        modifier = modifier
            .semantics { contentDescription = "${streak.days} ${message.caption}" }
            .background(background, RoundedCornerShape(22.dp))
            .let { base ->
                if (onClick != null) {
                    base.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    base
                }
            }
            .padding(start = 6.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
            if (streak.isActive) {
                StreakFlame(iconSize = 26.dp, ignitionId = ignitionId)
            } else {
                StreakIce(iconSize = 26.dp)
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = streak.days.toString(),
                color = accent,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = message.caption,
                color = accent,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StreakFlame(
    iconSize: Dp,
    modifier: Modifier = Modifier,
    ignitionId: Int = 0
) {
    val idle = rememberInfiniteTransition(label = "flameIdle")
    val flicker by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "flicker"
    )
    val inner by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "inner"
    )
    val sway by idle.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "sway"
    )
    val glow by idle.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val scale = remember { Animatable(1f) }
    val burst = remember { Animatable(1f) }
    LaunchedEffect(ignitionId) {
        if (ignitionId > 0) {
            scale.snapTo(0.3f)
            burst.snapTo(0f)
            launch {
                scale.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
            }
            burst.animateTo(1f, tween(1000, easing = LinearEasing))
        }
    }

    Box(modifier = modifier.size(iconSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.requiredSize(iconSize * 2.4f)) {
            val unit = iconSize.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FlameOuter.copy(alpha = glow * scale.value.coerceAtMost(1f)), Color.Transparent),
                    center = center,
                    radius = unit * 0.95f
                ),
                radius = unit * 0.95f,
                center = center
            )

            val progress = burst.value
            if (progress < 1f) {
                Sparks.forEach { spark ->
                    val angle = Math.toRadians(spark.angleDeg.toDouble())
                    val distance = unit * (0.35f + 0.9f * spark.reach * progress)

                    drawCircle(
                        color = spark.color.copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                        radius = unit * spark.radius * (1f - 0.5f * progress),
                        center = Offset(
                            x = center.x + (sin(angle) * distance).toFloat(),
                            y = center.y - (cos(angle) * distance).toFloat()
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    val enter = scale.value
                    scaleX = (1.05f - 0.10f * flicker) * enter
                    scaleY = (0.93f + 0.15f * flicker) * enter
                    rotationZ = sway * 3f
                    transformOrigin = TransformOrigin(0.5f, 0.95f)
                }
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = FlameOuter,
                modifier = Modifier.fillMaxSize()
            )

            // Núcleo amarillo, más chico y con su propio parpadeo.
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = FlameInner,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(iconSize * 0.55f)
                    .graphicsLayer {
                        scaleY = 0.9f + 0.2f * inner
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
            )
        }
    }
}

@Composable
fun StreakIce(
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val idle = rememberInfiniteTransition(label = "iceIdle")
    val shimmer by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "shimmer"
    )
    val pulse by idle.animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val sway by idle.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "iceSway"
    )

    // Al aparecer, el copo se "congela": crece con un rebote suave y una onda de escarcha se expande.
    val scale = remember { Animatable(0.5f) }
    val ring = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
        }
        ring.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    Box(modifier = modifier.size(iconSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.requiredSize(iconSize * 2.4f)) {
            val unit = iconSize.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(IceMain.copy(alpha = 0.28f * pulse), Color.Transparent),
                    center = center,
                    radius = unit * 0.9f
                ),
                radius = unit * 0.9f,
                center = center
            )

            val wave = ring.value
            if (wave < 1f) {
                drawCircle(
                    color = IceLight.copy(alpha = (1f - wave) * 0.9f),
                    radius = unit * (0.35f + 0.85f * wave),
                    center = center,
                    style = Stroke(width = unit * 0.06f)
                )
            }

            Twinkles.forEach { twinkle ->
                val phase = (shimmer + twinkle.phase) % 1f
                val strength = sin(PI * phase).toFloat().let { it * it }
                val angle = Math.toRadians(twinkle.angleDeg.toDouble())

                drawSparkle(
                    center = Offset(
                        x = center.x + (sin(angle) * unit * twinkle.distance).toFloat(),
                        y = center.y - (cos(angle) * unit * twinkle.distance).toFloat()
                    ),
                    radius = unit * twinkle.size * (0.6f + 0.4f * strength),
                    color = IceLight.copy(alpha = strength)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = sway * 5f
                    alpha = pulse
                }
        ) {
            // Halo claro detrás del copo para que parezca escarcha y no un ícono plano.
            Icon(
                imageVector = Icons.Rounded.AcUnit,
                contentDescription = null,
                tint = IceLight,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.18f
                        scaleY = 1.18f
                        alpha = 0.55f
                    }
            )

            Icon(
                imageVector = Icons.Rounded.AcUnit,
                contentDescription = null,
                tint = IceMain,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val strokeWidth = radius * 0.28f

    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

/** Explica la racha al tocar el marcador: cuántos días lleva y qué falta para mantenerla o encenderla. */
@Composable
fun StreakInfoDialog(
    streak: StudentStreak,
    onDismiss: () -> Unit
) {
    val message = streakMessage(streak)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = NovaSurface,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                    if (streak.isActive) {
                        StreakFlame(iconSize = 64.dp, ignitionId = 1)
                    } else {
                        StreakIce(iconSize = 64.dp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message.title,
                    color = NovaText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message.body,
                    color = NovaTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "Entendido", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
