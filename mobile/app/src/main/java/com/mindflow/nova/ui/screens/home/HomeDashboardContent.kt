package com.mindflow.nova.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.ui.components.NovaProgressBar
import com.mindflow.nova.ui.components.StreakBadge
import com.mindflow.nova.ui.components.StreakInfoDialog
import com.mindflow.nova.ui.screens.lessons.MissionState
import com.mindflow.nova.ui.screens.lessons.common.MechanicChip
import com.mindflow.nova.ui.screens.lessons.computeMissionStates
import com.mindflow.nova.ui.screens.lessons.currentMissionIndex
import com.mindflow.nova.ui.theme.NovaSurface
import com.mindflow.nova.ui.theme.NovaBlue
import com.mindflow.nova.ui.theme.NovaHeroGradient
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaLocked
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import kotlinx.coroutines.delay

@Composable
fun HomeDashboardContent(
    level: LevelResponse,
    progress: StudentProgress?,
    onMissionSelected: (MissionResponse) -> Unit,
    onOpenLessons: () -> Unit,
    modifier: Modifier = Modifier
) {
    val missions = level.missions
    val completedMissionIds = progress?.completedMissionIds?.toSet().orEmpty()
    val missionStates = computeMissionStates(missions, completedMissionIds)
    val currentIndex = currentMissionIndex(missionStates)
    val nextMission = missions.getOrNull(currentIndex)
    val completedCount = missionStates.count { it.isCompleted }
    val levelProgressPercentage = progress?.levels
        ?.firstOrNull { it.id == level.id }
        ?.progressPercentage
        ?: 0.0

    var showStreakInfo by remember { mutableStateOf(false) }
    val streak = progress?.streak

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Sin racha todavía (cargando) no se muestra nada, para que no
            // parpadee un hielo que enseguida se vuelve llama.
            if (streak != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    StreakBadge(streak = streak, onClick = { showStreakInfo = true })
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            WelcomeBanner()
        }

        item {
            CurrentLevelCard(level = level, progressPercentage = levelProgressPercentage)
        }

        item {
            SectionHeader(
                title = "Ruta de aprendizaje",
                subtitle = "$completedCount de ${missions.size} misiones completadas"
            )
        }

        itemsIndexed(missions) { index, mission ->
            MissionRouteRow(
                mission = mission,
                index = index,
                state = missionStates[index],
                isCurrent = index == currentIndex,
                onClick = { onMissionSelected(mission) }
            )
        }

        if (nextMission == null && missions.isNotEmpty()) {
            item {
                RouteCompleteCard(onClick = onOpenLessons)
            }
        }

        item {
            Spacer(modifier = Modifier.height(22.dp))
        }
    }

    if (showStreakInfo && streak != null) {
        StreakInfoDialog(streak = streak, onDismiss = { showStreakInfo = false })
    }
}

@Composable
private fun WelcomeBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(NovaHeroGradient)
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Bienvenido a NOVA",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Avanza en misiones sobre igualdad, justicia, dignidad y respeto.",
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            MascotaBadge()
        }
    }
}

@Composable
private fun MascotaBadge() {
    Surface(
        modifier = Modifier.size(86.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Mascota",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CurrentLevelCard(level: LevelResponse, progressPercentage: Double) {
    // Fondo lila suave en vez de blanco: distingue esta tarjeta "resumen" de
    // las filas de misión de abajo, que sí son blancas — si no, todo el
    // dashboard es la misma tarjeta blanca repetida.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NovaLightPurple,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(18.dp),
                color = NovaSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "01",
                        color = NovaPurple,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = level.name,
                    color = NovaText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = level.description ?: "Nivel educativo disponible",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                NovaProgressBar(progress = (progressPercentage / 100.0).toFloat())
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${progressPercentage.toInt()}%",
                color = NovaPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = NovaText,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = subtitle,
            color = NovaTextSecondary,
            fontSize = 13.sp
        )
    }
}

/** Cierre de la ruta cuando ya no queda ninguna misión pendiente: lleva al mapa para repasar. */
@Composable
private fun RouteCompleteCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NovaSurface,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(NovaHeroGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡Completaste toda la ruta!",
                    color = NovaPurple,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Repasa las lecciones cuando quieras.",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = NovaBlue,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun MissionRouteRow(
    mission: MissionResponse,
    index: Int,
    state: MissionState,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    // Cada fila entra con un pequeño retraso escalonado según su posición.
    // Se anima con graphicsLayer (y no con AnimatedVisibility) para que la
    // fila ocupe su espacio desde el inicio y el resto no "salte" al aparecer.
    var visible by remember(mission.id) { mutableStateOf(false) }
    LaunchedEffect(mission.id) {
        delay(index * 70L)
        visible = true
    }
    val enter by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "missionRowEnter"
    )

    Surface(
        onClick = onClick,
        enabled = state.isUnlocked,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enter
                translationY = (1f - enter) * size.height / 4f
            },
        shape = RoundedCornerShape(20.dp),
        color = NovaSurface,
        // La misión que sigue se distingue con un borde degradé (sin sombra, para
        // no juntar borde y sombra en la misma tarjeta); las demás, solo sombra.
        border = if (isCurrent) BorderStroke(2.dp, NovaHeroGradient) else null,
        shadowElevation = if (isCurrent) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            MissionRouteIndicator(index = index, state = state, isCurrent = isCurrent)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isCurrent) {
                    Text(
                        text = "Sigue transformando tu aprendizaje",
                        color = NovaPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = "${index + 1}. ${mission.title}",
                    color = if (state.isUnlocked) NovaText else NovaTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mission.description ?: "Misión educativa de NOVA",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (isCurrent) {
                    Spacer(modifier = Modifier.height(10.dp))

                    MechanicChip(mechanic = mission.mechanic)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovaPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(text = "Continuar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionRouteIndicator(index: Int, state: MissionState, isCurrent: Boolean) {
    val shape = RoundedCornerShape(12.dp)
    val background: Brush = when {
        state.isCompleted -> SolidColor(NovaLightPurple)
        isCurrent -> NovaHeroGradient
        state.isUnlocked -> SolidColor(NovaPurple)
        else -> SolidColor(NovaLocked)
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(background)
            .let { base ->
                if (state.isCompleted) base.border(1.5.dp, NovaPurple, shape) else base
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isCompleted -> Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Misión completada",
                tint = NovaPurple
            )

            !state.isUnlocked -> Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Misión bloqueada",
                tint = NovaTextSecondary,
                modifier = Modifier.size(20.dp)
            )

            else -> Text(
                text = "%02d".format(index + 1),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}
