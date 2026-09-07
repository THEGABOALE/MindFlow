package com.mindflow.nova.ui.screens.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

// --- Tipos de contenido que consumen las pantallas de lección ---
// (antes vivían en los *MockData.kt; ahora el contenido llega del backend).

data class LessonOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val feedback: String
)

data class LessonQuestion(
    val id: Int,
    val prompt: String,
    val options: List<LessonOption>
)

data class MatchingPair(
    val id: Int,
    val term: String,
    val match: String
)

data class TrueFalseQuestion(
    val id: Int,
    val statement: String,
    val correctAnswer: Boolean,
    val explanation: String
)

// --- Mappers del contenido del backend a los tipos de cada mecánica ---

/** Opción múltiple: cada pregunta con sus opciones y el feedback de cada una. */
fun MissionContent.toLessonQuestions(): List<LessonQuestion> =
    questions.map { question ->
        LessonQuestion(
            id = question.id,
            prompt = question.prompt,
            options = question.options.map { option ->
                LessonOption(
                    id = option.id,
                    text = option.text,
                    isCorrect = option.isCorrect,
                    // Si la opción no trae su propio feedback, cae al de la pregunta.
                    feedback = option.feedback ?: question.feedback.orEmpty()
                )
            }
        )
    }

/** Relación de conceptos: la misión trae una sola pregunta con todos los pares. */
fun MissionContent.toMatchingPairs(): List<MatchingPair> =
    questions.flatMap { it.pairs }.map { pair ->
        MatchingPair(id = pair.id, term = pair.term, match = pair.match)
    }

/**
 * Verdadero/falso: el enunciado es el prompt, la explicación va en el feedback
 * de la pregunta, y cuál es la respuesta correcta se deduce de qué opción
 * ("Verdadero"/"Falso") está marcada como correcta en la base.
 */
fun MissionContent.toTrueFalseQuestions(): List<TrueFalseQuestion> =
    questions.map { question ->
        val correctIsTrue = question.options
            .firstOrNull { it.isCorrect }
            ?.text
            ?.trim()
            .equals("Verdadero", ignoreCase = true)

        TrueFalseQuestion(
            id = question.id,
            statement = question.prompt,
            correctAnswer = correctIsTrue,
            explanation = question.feedback.orEmpty()
        )
    }

private sealed class LessonContentState {
    object Loading : LessonContentState()
    data class Ready(val content: MissionContent) : LessonContentState()
    data class Error(val message: String) : LessonContentState()
}

/**
 * Carga el contenido de la misión desde el backend y, según su mecánica,
 * muestra la pantalla de lección que corresponde. Las mecánicas que todavía no
 * tienen pantalla propia (por ejemplo la sopa de letras) van al placeholder sin
 * necesidad de pedir contenido.
 */
@Composable
fun LessonHost(mission: MissionResponse, onExit: () -> Unit) {
    val mechanic = mission.mechanic

    if (mechanic != "multiple_choice" && mechanic != "matching" && mechanic != "true_false") {
        MiniGamePlaceholderScreen(mission = mission, onBack = onExit)
        return
    }

    var state by remember(mission.id) { mutableStateOf<LessonContentState>(LessonContentState.Loading) }

    LaunchedEffect(mission.id) {
        state = try {
            val response = RetrofitClient.api.getMissionContent(mission.id)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                LessonContentState.Ready(body.mission)
            } else {
                LessonContentState.Error("No se pudo cargar la misión (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            LessonContentState.Error("Error de conexión: ${e.message}")
        }
    }

    when (val current = state) {
        LessonContentState.Loading -> LessonContentLoading()

        is LessonContentState.Error -> LessonContentError(message = current.message, onExit = onExit)

        is LessonContentState.Ready -> when (mechanic) {
            "multiple_choice" -> LessonPlayScreen(
                mission = mission,
                questions = current.content.toLessonQuestions(),
                onExit = onExit
            )

            "matching" -> MatchingLessonScreen(
                mission = mission,
                pairs = current.content.toMatchingPairs(),
                onExit = onExit
            )

            "true_false" -> TrueFalseLessonScreen(
                mission = mission,
                questions = current.content.toTrueFalseQuestions(),
                onExit = onExit
            )

            else -> MiniGamePlaceholderScreen(mission = mission, onBack = onExit)
        }
    }
}

@Composable
private fun LessonContentLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NovaPurple)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cargando la misión...",
                color = NovaTextSecondary,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun LessonContentError(message: String, onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No se pudo cargar la misión",
                color = NovaText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = NovaTextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onExit) {
                Text("Volver")
            }
        }
    }
}
