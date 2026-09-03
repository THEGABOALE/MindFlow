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
import androidx.compose.foundation.shape.CircleShape
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
import com.mindflow.nova.ui.screens.lessons.common.LESSON_MAX_PLUMAS
import com.mindflow.nova.ui.screens.lessons.common.LESSON_SEMILLAS_REWARD
import com.mindflow.nova.ui.screens.lessons.common.LessonCompletedScreen
import com.mindflow.nova.ui.screens.lessons.common.LessonEndScreen
import com.mindflow.nova.ui.screens.lessons.common.ExitConfirmationDialog
import com.mindflow.nova.ui.screens.lessons.common.LessonTopBar
import com.mindflow.nova.ui.screens.lessons.common.MascotaPlaceholder
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

private enum class QuestionPhase { ANSWERING, ANSWERED }
private enum class LessonStage { IN_PROGRESS, OUT_OF_PLUMAS, COMPLETED }

/**
 * Lección de preguntas de opción múltiple (Lección 1 - "Bienvenida a NOVA").
 */
@Composable
fun LessonPlayScreen(
    mission: MissionResponse,
    questions: List<LessonQuestion>,
    onExit: () -> Unit
) {
    // Las plumas las define el backend por misión; la constante solo es
    // respaldo por si la API todavía no manda el campo.
    val maxPlumas = mission.maxPlumas ?: LESSON_MAX_PLUMAS

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOptionId by remember { mutableStateOf<Int?>(null) }
    var phase by remember { mutableStateOf(QuestionPhase.ANSWERING) }
    var plumas by remember { mutableStateOf(maxPlumas) }
    var correctCount by remember { mutableStateOf(0) }
    var stage by remember { mutableStateOf(LessonStage.IN_PROGRESS) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    fun resetLesson() {
        currentIndex = 0
        selectedOptionId = null
        phase = QuestionPhase.ANSWERING
        plumas = maxPlumas
        correctCount = 0
        stage = LessonStage.IN_PROGRESS
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
    ) {
        when (stage) {
            LessonStage.COMPLETED -> {
                LessonCompletedScreen(
                    subtitle = "$correctCount de ${questions.size} preguntas correctas",
                    rewardAmount = LESSON_SEMILLAS_REWARD,
                    onContinue = onExit
                )
            }

            LessonStage.OUT_OF_PLUMAS -> {
                LessonEndScreen(
                    title = "¡Te quedaste sin plumas!",
                    message = "Necesitas plumas para seguir en la lección",
                    primaryLabel = "Reintentar nivel",
                    onPrimary = { resetLesson() },
                    secondaryLabel = "Volver al inicio",
                    onSecondary = onExit
                )
            }

            LessonStage.IN_PROGRESS -> {
                val question = questions[currentIndex]
                val selectedOption = question.options.firstOrNull { it.id == selectedOptionId }
                val progress = when (phase) {
                    QuestionPhase.ANSWERING -> currentIndex.toFloat() / questions.size
                    QuestionPhase.ANSWERED -> (currentIndex + 1).toFloat() / questions.size
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    LessonTopBar(
                        progress = progress,
                        plumas = plumas,
                        justLostPluma = phase == QuestionPhase.ANSWERED && selectedOption?.isCorrect == false,
                        onClose = { showExitConfirmation = true },
                        maxPlumas = maxPlumas
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (phase == QuestionPhase.ANSWERING) {
                        QuestionHeader(prompt = question.prompt)

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            question.options.forEach { option ->
                                AnswerOptionRow(
                                    text = option.text,
                                    isSelected = option.id == selectedOptionId,
                                    onClick = { selectedOptionId = option.id }
                                )
                            }
                        }
                    } else {
                        LessonResultReveal(
                            question = question,
                            selectedOptionId = selectedOptionId
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val buttonLabel = when {
                        phase == QuestionPhase.ANSWERING -> "Continuar"
                        currentIndex == questions.lastIndex -> "Ver resultados"
                        else -> "Siguiente"
                    }

                    Button(
                        onClick = {
                            if (phase == QuestionPhase.ANSWERING) {
                                if (selectedOption?.isCorrect == true) {
                                    correctCount++
                                } else {
                                    plumas = (plumas - 1).coerceAtLeast(0)
                                }
                                phase = QuestionPhase.ANSWERED
                            } else {
                                when {
                                    plumas == 0 -> stage = LessonStage.OUT_OF_PLUMAS
                                    currentIndex == questions.lastIndex -> stage = LessonStage.COMPLETED
                                    else -> {
                                        currentIndex++
                                        selectedOptionId = null
                                        phase = QuestionPhase.ANSWERING
                                    }
                                }
                            }
                        },
                        enabled = selectedOptionId != null,
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

@Composable
private fun QuestionHeader(prompt: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MascotaPlaceholder(modifier = Modifier.size(72.dp))

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF0EDF2)
        ) {
            Text(
                text = prompt,
                modifier = Modifier.padding(14.dp),
                color = NovaText,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun AnswerOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) NovaText else Color(0xFFF0EDF2)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (isSelected) Color.White else NovaTextSecondary, CircleShape)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = text,
                color = if (isSelected) Color.White else NovaText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun LessonResultReveal(
    question: LessonQuestion,
    selectedOptionId: Int?
) {
    val selectedOption = question.options.firstOrNull { it.id == selectedOptionId }

    Column {
        Row(verticalAlignment = Alignment.Top) {
            MascotaPlaceholder(modifier = Modifier.size(96.dp))

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                modifier = Modifier.weight(1f),
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
                        text = selectedOption?.feedback ?: "",
                        color = NovaText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            question.options.forEach { option ->
                val suffix = when {
                    option.isCorrect -> "Respuesta correcta"
                    option.id == selectedOptionId -> "Respuesta incorrecta (elegida)"
                    else -> "Respuesta incorrecta"
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0EDF2)
                ) {
                    Text(
                        text = "${option.text} — $suffix",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = NovaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
