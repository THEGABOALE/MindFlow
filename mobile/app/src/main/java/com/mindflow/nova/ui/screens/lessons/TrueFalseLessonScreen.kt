package com.mindflow.nova.ui.screens.lessons

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.mindflow.nova.ui.screens.lessons.common.LESSON_SEMILLAS_REWARD
import com.mindflow.nova.ui.screens.lessons.common.LessonCompletedScreen
import com.mindflow.nova.ui.screens.lessons.common.LessonEndScreen
import com.mindflow.nova.ui.screens.lessons.common.LessonTopBar
import com.mindflow.nova.ui.screens.lessons.common.MascotaPlaceholder
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

private enum class TruthPhase { ANSWERING, ANSWERED }
private enum class TruthStage { IN_PROGRESS, OUT_OF_PLUMAS, COMPLETED }

private val TrueFalseIdle = Color(0xFFDCEBFB)
private val TrueFalseSelected = Color(0xFF5B93C7)
private val TrueFalseCorrect = Color(0xFF2E9E5B)
private val TrueFalseWrong = Color(0xFFC0392B)

/**
 * Cuestionario de verdadero/falso ("Decisiones con respeto"): "¿Tú qué crees?"
 */
@Composable
fun TrueFalseLessonScreen(
    mission: MissionResponse,
    questions: List<TrueFalseQuestion>,
    onExit: () -> Unit
) {
    // Las plumas las define el backend por misión; la constante solo es
    // respaldo por si la API todavía no manda el campo.
    val maxPlumas = mission.maxPlumas ?: LESSON_MAX_PLUMAS

    var currentIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Boolean?>(null) }
    var phase by remember { mutableStateOf(TruthPhase.ANSWERING) }
    var plumas by remember { mutableStateOf(maxPlumas) }
    var correctCount by remember { mutableStateOf(0) }
    var stage by remember { mutableStateOf(TruthStage.IN_PROGRESS) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    fun resetLesson() {
        currentIndex = 0
        selectedAnswer = null
        phase = TruthPhase.ANSWERING
        plumas = maxPlumas
        correctCount = 0
        stage = TruthStage.IN_PROGRESS
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
    ) {
        when (stage) {
            TruthStage.COMPLETED -> {
                LessonCompletedScreen(
                    subtitle = "$correctCount de ${questions.size} afirmaciones correctas",
                    rewardAmount = LESSON_SEMILLAS_REWARD,
                    onContinue = onExit
                )
            }

            TruthStage.OUT_OF_PLUMAS -> {
                LessonEndScreen(
                    title = "¡Te quedaste sin plumas!",
                    message = "Necesitas plumas para seguir en la lección",
                    primaryLabel = "Reintentar nivel",
                    onPrimary = { resetLesson() },
                    secondaryLabel = "Volver al inicio",
                    onSecondary = onExit
                )
            }

            TruthStage.IN_PROGRESS -> {
                val question = questions[currentIndex]
                val progress = when (phase) {
                    TruthPhase.ANSWERING -> currentIndex.toFloat() / questions.size
                    TruthPhase.ANSWERED -> (currentIndex + 1).toFloat() / questions.size
                }
                val isCorrectSelection = selectedAnswer == question.correctAnswer

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    LessonTopBar(
                        progress = progress,
                        plumas = plumas,
                        justLostPluma = phase == TruthPhase.ANSWERED && !isCorrectSelection,
                        onClose = { showExitConfirmation = true },
                        maxPlumas = maxPlumas
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MascotaPlaceholder(modifier = Modifier.size(72.dp))

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF0EDF2)
                        ) {
                            Text(
                                text = "¿Tú qué crees?",
                                modifier = Modifier.padding(14.dp),
                                color = NovaText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = NovaLightPurple
                    ) {
                        Text(
                            text = question.statement,
                            modifier = Modifier.padding(16.dp),
                            color = NovaText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (phase == TruthPhase.ANSWERED) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF0EDF2)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Mascota explica:",
                                    color = NovaPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = question.explanation,
                                    color = NovaText,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TrueFalseButton(
                            text = "VERDADERO",
                            modifier = Modifier.weight(1f),
                            state = truthButtonState(
                                phase = phase,
                                thisValue = true,
                                selected = selectedAnswer,
                                correctAnswer = question.correctAnswer
                            ),
                            onClick = { if (phase == TruthPhase.ANSWERING) selectedAnswer = true }
                        )

                        TrueFalseButton(
                            text = "FALSO",
                            modifier = Modifier.weight(1f),
                            state = truthButtonState(
                                phase = phase,
                                thisValue = false,
                                selected = selectedAnswer,
                                correctAnswer = question.correctAnswer
                            ),
                            onClick = { if (phase == TruthPhase.ANSWERING) selectedAnswer = false }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val buttonLabel = when {
                        phase == TruthPhase.ANSWERING -> "Continuar"
                        currentIndex == questions.lastIndex -> "Ver resultados"
                        else -> "Siguiente"
                    }

                    Button(
                        onClick = {
                            if (phase == TruthPhase.ANSWERING) {
                                if (isCorrectSelection) {
                                    correctCount++
                                } else {
                                    plumas = (plumas - 1).coerceAtLeast(0)
                                }
                                phase = TruthPhase.ANSWERED
                            } else {
                                when {
                                    plumas == 0 -> stage = TruthStage.OUT_OF_PLUMAS
                                    currentIndex == questions.lastIndex -> stage = TruthStage.COMPLETED
                                    else -> {
                                        currentIndex++
                                        selectedAnswer = null
                                        phase = TruthPhase.ANSWERING
                                    }
                                }
                            }
                        },
                        enabled = selectedAnswer != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovaText,
                            contentColor = Color.White,
                            disabledContainerColor = NovaBorder,
                            disabledContentColor = NovaTextSecondary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(text = buttonLabel, fontWeight = FontWeight.Bold)
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

private enum class TrueFalseButtonState { IDLE, SELECTED, CORRECT, WRONG }

private fun truthButtonState(
    phase: TruthPhase,
    thisValue: Boolean,
    selected: Boolean?,
    correctAnswer: Boolean
): TrueFalseButtonState {
    if (phase == TruthPhase.ANSWERING) {
        return if (selected == thisValue) TrueFalseButtonState.SELECTED else TrueFalseButtonState.IDLE
    }
    if (selected != thisValue) return TrueFalseButtonState.IDLE
    return if (thisValue == correctAnswer) TrueFalseButtonState.CORRECT else TrueFalseButtonState.WRONG
}

@Composable
private fun TrueFalseButton(
    text: String,
    state: TrueFalseButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when (state) {
        TrueFalseButtonState.IDLE -> TrueFalseIdle
        TrueFalseButtonState.SELECTED -> TrueFalseSelected
        TrueFalseButtonState.CORRECT -> TrueFalseCorrect
        TrueFalseButtonState.WRONG -> TrueFalseWrong
    }
    val contentColor = if (state == TrueFalseButtonState.IDLE) NovaText else Color.White

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = background
    ) {
        Box(
            modifier = Modifier.padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
