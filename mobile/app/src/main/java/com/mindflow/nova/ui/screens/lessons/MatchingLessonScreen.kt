package com.mindflow.nova.ui.screens.lessons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.ui.screens.lessons.common.ExitConfirmationDialog
import com.mindflow.nova.ui.screens.lessons.common.LESSON_MAX_PLUMAS
import com.mindflow.nova.ui.screens.lessons.common.LessonCompletedScreen
import com.mindflow.nova.ui.screens.lessons.common.LessonEndScreen
import com.mindflow.nova.ui.screens.lessons.common.LessonTopBar
import com.mindflow.nova.ui.screens.lessons.common.MascotaPlaceholder
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import kotlinx.coroutines.delay

private const val MATCHING_TIME_SECONDS = 45
private const val SEMILLAS_PER_ACIERTO = 5

private enum class MatchingStage { IN_PROGRESS, OUT_OF_PLUMAS, TIME_UP, COMPLETED }

private enum class ItemState { IDLE, SELECTED, CORRECT, WRONG }

/**
 * Minijuego de relación de conceptos ("Reconocer mis derechos"): emparejar
 * cada término con su palabra, contra un cronómetro y con plumas de vida.
 */
@Composable
fun MatchingLessonScreen(
    mission: MissionResponse,
    pairs: List<MatchingPair>,
    onExit: () -> Unit
) {
    // El límite de tiempo y las plumas los define el backend por misión; las
    // constantes solo son respaldo por si la API todavía no manda los campos.
    val maxPlumas = mission.maxPlumas ?: LESSON_MAX_PLUMAS
    val timeLimitSeconds = mission.timeLimitSeconds ?: MATCHING_TIME_SECONDS

    var resetKey by remember { mutableStateOf(0) }
    var matchedIds by remember(resetKey) { mutableStateOf(setOf<Int>()) }
    var selectedTermId by remember(resetKey) { mutableStateOf<Int?>(null) }
    var wrongPair by remember(resetKey) { mutableStateOf<Pair<Int, Int>?>(null) }
    var correctCount by remember(resetKey) { mutableStateOf(0) }
    var wrongCount by remember(resetKey) { mutableStateOf(0) }
    var plumas by remember(resetKey) { mutableStateOf(maxPlumas) }
    var stage by remember(resetKey) { mutableStateOf(MatchingStage.IN_PROGRESS) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var secondsLeft by remember(resetKey) { mutableStateOf(timeLimitSeconds) }
    var feedback by remember(resetKey) { mutableStateOf("¡Vas bien! Elegí un par") }
    var justLostPluma by remember(resetKey) { mutableStateOf(false) }

    val shuffledTerms = remember(resetKey) { pairs.shuffled() }
    val shuffledMatches = remember(resetKey) { pairs.shuffled() }

    LaunchedEffect(resetKey) {
        while (secondsLeft > 0 && stage == MatchingStage.IN_PROGRESS) {
            delay(1000)
            secondsLeft -= 1
        }
        if (secondsLeft <= 0 && stage == MatchingStage.IN_PROGRESS) {
            stage = MatchingStage.TIME_UP
        }
    }

    LaunchedEffect(wrongPair) {
        if (wrongPair != null) {
            delay(700)
            wrongPair = null
            selectedTermId = null
        }
    }

    fun onMatchTap(matchPairId: Int) {
        val termId = selectedTermId ?: return
        if (termId == matchPairId) {
            matchedIds = matchedIds + termId
            correctCount++
            feedback = "¡Correcto!"
            selectedTermId = null
            if (matchedIds.size == pairs.size) {
                stage = MatchingStage.COMPLETED
            }
        } else {
            wrongCount++
            justLostPluma = true
            plumas = (plumas - 1).coerceAtLeast(0)
            feedback = "Casi... -1 pluma"
            wrongPair = termId to matchPairId
            if (plumas == 0) {
                stage = MatchingStage.OUT_OF_PLUMAS
            }
        }
    }

    fun onTermTap(termId: Int) {
        selectedTermId = termId
        justLostPluma = false
        feedback = "Ahora tocá la palabra que combine"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
    ) {
        when (stage) {
            MatchingStage.COMPLETED -> {
                LessonCompletedScreen(
                    subtitle = "Emparejaste ${pairs.size} de ${pairs.size} conceptos",
                    rewardAmount = correctCount * SEMILLAS_PER_ACIERTO,
                    rewardLabel = "semillas (según tus aciertos)",
                    onContinue = onExit,
                    extraContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ResultStat(label = "aciertos", value = correctCount, color = NovaPurple, background = Color(0xFFE6F7EA))
                            ResultStat(label = "fallos", value = wrongCount, color = Color(0xFFC0392B), background = Color(0xFFFBE7E5))
                        }
                    }
                )
            }

            MatchingStage.TIME_UP -> {
                LessonEndScreen(
                    title = "¡Se acabó el tiempo!",
                    message = "Completaste ${matchedIds.size} de ${pairs.size} pares antes de que se acabara",
                    primaryLabel = "Reintentar minijuego",
                    onPrimary = { resetKey++ },
                    secondaryLabel = "Volver al inicio",
                    onSecondary = onExit
                )
            }

            MatchingStage.OUT_OF_PLUMAS -> {
                LessonEndScreen(
                    title = "¡Te quedaste sin plumas!",
                    message = "Necesitas plumas para seguir en el minijuego",
                    primaryLabel = "Reintentar minijuego",
                    onPrimary = { resetKey++ },
                    secondaryLabel = "Volver al inicio",
                    onSecondary = onExit
                )
            }

            MatchingStage.IN_PROGRESS -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    LessonTopBar(
                        progress = matchedIds.size.toFloat() / pairs.size,
                        plumas = plumas,
                        justLostPluma = justLostPluma,
                        onClose = { showExitConfirmation = true },
                        maxPlumas = maxPlumas,
                        trailingBadge = { TimerBadge(secondsLeft = secondsLeft) }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Marca cada concepto con su palabra",
                        color = NovaText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            shuffledTerms.forEach { pair ->
                                val state = when {
                                    matchedIds.contains(pair.id) -> ItemState.CORRECT
                                    wrongPair?.first == pair.id -> ItemState.WRONG
                                    selectedTermId == pair.id -> ItemState.SELECTED
                                    else -> ItemState.IDLE
                                }
                                MatchingItemCard(
                                    text = pair.term,
                                    state = state,
                                    onClick = { if (state == ItemState.IDLE) onTermTap(pair.id) }
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            shuffledMatches.forEach { pair ->
                                val state = when {
                                    matchedIds.contains(pair.id) -> ItemState.CORRECT
                                    wrongPair?.second == pair.id -> ItemState.WRONG
                                    else -> ItemState.IDLE
                                }
                                MatchingItemCard(
                                    text = pair.match,
                                    state = state,
                                    onClick = {
                                        if (state == ItemState.IDLE && selectedTermId != null) {
                                            onMatchTap(pair.id)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF0EDF2)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MascotaPlaceholder(modifier = Modifier.size(32.dp), label = "🙂")
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = feedback,
                                color = NovaText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        if (showExitConfirmation) {
            ExitConfirmationDialog(
                onStay = { showExitConfirmation = false },
                onExit = onExit
            )
        }
    }
}

@Composable
private fun TimerBadge(secondsLeft: Int) {
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (secondsLeft <= 10) Color(0xFFC0392B) else NovaBorder)
    ) {
        Text(
            text = "%d:%02d".format(minutes, seconds),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (secondsLeft <= 10) Color(0xFFC0392B) else NovaText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MatchingItemCard(
    text: String,
    state: ItemState,
    onClick: () -> Unit
) {
    val background = when (state) {
        ItemState.CORRECT -> Color(0xFFE6F7EA)
        ItemState.WRONG -> Color(0xFFFBE7E5)
        else -> Color.White
    }
    val border = when (state) {
        ItemState.CORRECT -> Color(0xFF2E9E5B)
        ItemState.WRONG -> Color(0xFFC0392B)
        ItemState.SELECTED -> NovaText
        ItemState.IDLE -> NovaBorder
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = state == ItemState.IDLE, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = background,
        border = BorderStroke(1.5.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = NovaText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        when (state) {
                            ItemState.CORRECT -> Color(0xFF2E9E5B)
                            ItemState.WRONG -> Color(0xFFC0392B)
                            else -> Color.Transparent
                        },
                        CircleShape
                    )
                    .border(1.5.dp, if (state == ItemState.SELECTED) NovaText else border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (state == ItemState.SELECTED) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NovaText, CircleShape)
                    )
                } else if (state == ItemState.CORRECT) {
                    Text(text = "✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else if (state == ItemState.WRONG) {
                    Text(text = "✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResultStat(label: String, value: Int, color: Color, background: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value.toString(), color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(text = label, color = color, fontSize = 11.sp)
        }
    }
}
