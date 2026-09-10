package com.mindflow.nova.ui.screens.lessons

import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionOption
import com.mindflow.nova.data.model.MissionPair
import com.mindflow.nova.data.model.MissionQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de los mappers que convierten el contenido crudo del backend
 * (MissionContent) a los tipos que consumen las pantallas de lección. Son
 * funciones puras sobre data classes, así que corren como tests de JVM sin
 * necesitar emulador ni Android en absoluto.
 */
class LessonContentTest {

    private fun missionContent(
        mechanic: String,
        questions: List<MissionQuestion>
    ) = MissionContent(
        id = 1,
        levelId = 1,
        title = "Misión de prueba",
        description = null,
        topic = null,
        orderIndex = 1,
        pointsReward = 100,
        mechanic = mechanic,
        timeLimitSeconds = null,
        maxPlumas = 3,
        questions = questions
    )

    @Test
    fun `toLessonQuestions mapea preguntas y opciones tal cual vienen`() {
        val content = missionContent(
            mechanic = "multiple_choice",
            questions = listOf(
                MissionQuestion(
                    id = 1,
                    prompt = "¿Qué busca enseñar NOVA?",
                    type = "multiple_choice",
                    feedback = "Feedback de la pregunta",
                    orderIndex = 1,
                    points = 1,
                    options = listOf(
                        MissionOption(id = 10, text = "Correcta", isCorrect = true, feedback = "¡Bien!", orderIndex = 1),
                        MissionOption(id = 11, text = "Incorrecta", isCorrect = false, feedback = null, orderIndex = 2)
                    )
                )
            )
        )

        val questions = content.toLessonQuestions()

        assertEquals(1, questions.size)
        assertEquals("¿Qué busca enseñar NOVA?", questions[0].prompt)
        assertEquals(2, questions[0].options.size)

        val correct = questions[0].options.first { it.isCorrect }
        assertEquals("¡Bien!", correct.feedback)

        // Si la opción no trae su propio feedback, cae al de la pregunta.
        val wrong = questions[0].options.first { !it.isCorrect }
        assertEquals("Feedback de la pregunta", wrong.feedback)
    }

    @Test
    fun `toMatchingPairs junta los pares de todas las preguntas`() {
        val content = missionContent(
            mechanic = "matching",
            questions = listOf(
                MissionQuestion(
                    id = 5,
                    prompt = "Relacioná cada concepto",
                    type = "matching",
                    feedback = null,
                    orderIndex = 1,
                    points = 9,
                    pairs = listOf(
                        MissionPair(id = 100, term = "Igualdad", match = "Mismos derechos", orderIndex = 1),
                        MissionPair(id = 101, term = "Dignidad", match = "Respeto", orderIndex = 2)
                    )
                )
            )
        )

        val pairs = content.toMatchingPairs()

        assertEquals(2, pairs.size)
        assertEquals("Igualdad", pairs[0].term)
        assertEquals("Mismos derechos", pairs[0].match)
    }

    @Test
    fun `matchingQuestionId devuelve el id de la unica pregunta`() {
        val content = missionContent(
            mechanic = "matching",
            questions = listOf(
                MissionQuestion(id = 5, prompt = "x", type = "matching", feedback = null, orderIndex = 1, points = 9)
            )
        )

        assertEquals(5, content.matchingQuestionId())
    }

    @Test
    fun `matchingQuestionId da null si no hay preguntas`() {
        val content = missionContent(mechanic = "matching", questions = emptyList())

        assertNull(content.matchingQuestionId())
    }

    @Test
    fun `toTrueFalseQuestions detecta cual opcion es la correcta y sus IDs`() {
        val content = missionContent(
            mechanic = "true_false",
            questions = listOf(
                MissionQuestion(
                    id = 4,
                    prompt = "Afirmación de prueba",
                    type = "true_false",
                    feedback = "Explicación",
                    orderIndex = 1,
                    points = 1,
                    options = listOf(
                        MissionOption(id = 40, text = "Verdadero", isCorrect = false, feedback = null, orderIndex = 1),
                        MissionOption(id = 41, text = "Falso", isCorrect = true, feedback = null, orderIndex = 2)
                    )
                )
            )
        )

        val questions = content.toTrueFalseQuestions()

        assertEquals(1, questions.size)
        val question = questions[0]

        assertEquals("Afirmación de prueba", question.statement)
        assertEquals("Explicación", question.explanation)
        assertEquals(false, question.correctAnswer)
        assertEquals(40, question.trueOptionId)
        assertEquals(41, question.falseOptionId)
    }

    @Test
    fun `toTrueFalseQuestions con Verdadero correcto marca correctAnswer en true`() {
        val content = missionContent(
            mechanic = "true_false",
            questions = listOf(
                MissionQuestion(
                    id = 4,
                    prompt = "x",
                    type = "true_false",
                    feedback = null,
                    orderIndex = 1,
                    points = 1,
                    options = listOf(
                        MissionOption(id = 40, text = "Verdadero", isCorrect = true, feedback = null, orderIndex = 1),
                        MissionOption(id = 41, text = "Falso", isCorrect = false, feedback = null, orderIndex = 2)
                    )
                )
            )
        )

        assertTrue(content.toTrueFalseQuestions()[0].correctAnswer)
    }
}
