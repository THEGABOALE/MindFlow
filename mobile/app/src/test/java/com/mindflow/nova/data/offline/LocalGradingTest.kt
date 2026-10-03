package com.mindflow.nova.data.offline

import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionOption
import com.mindflow.nova.data.model.MissionPair
import com.mindflow.nova.data.model.MissionQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La calificación del teléfono tiene que dar lo mismo que la del servidor
 * (backend/tests/mission-grading.service.test.js): si no, el estudiante ve un
 * resultado sin conexión y otro distinto cuando se sube.
 */
class LocalGradingTest {

    private fun choiceQuestion(id: Int, correctId: Int, wrongId: Int) = MissionQuestion(
        id = id,
        prompt = "Pregunta $id",
        type = "multiple_choice",
        feedback = null,
        orderIndex = id,
        points = 1,
        options = listOf(
            MissionOption(id = correctId, text = "Bien", isCorrect = true, feedback = null, orderIndex = 1),
            MissionOption(id = wrongId, text = "Mal", isCorrect = false, feedback = null, orderIndex = 2)
        )
    )

    private fun matchingQuestion(id: Int, pairIds: List<Int>) = MissionQuestion(
        id = id,
        prompt = "Relaciona",
        type = "matching",
        feedback = null,
        orderIndex = id,
        points = 1,
        pairs = pairIds.mapIndexed { i, pairId -> MissionPair(pairId, "T$pairId", "M$pairId", i + 1) }
    )

    private fun content(
        questions: List<MissionQuestion>,
        timeLimitSeconds: Int? = null,
        maxPlumas: Int? = 3,
        pointsReward: Int = 100
    ) = MissionContent(
        id = 1,
        levelId = 1,
        title = "Misión de prueba",
        description = null,
        topic = null,
        orderIndex = 1,
        pointsReward = pointsReward,
        mechanic = "multiple_choice",
        timeLimitSeconds = timeLimitSeconds,
        maxPlumas = maxPlumas,
        questions = questions
    )

    // Dos preguntas de opción múltiple: 10 y 20 son las correctas.
    private val twoChoices = content(listOf(choiceQuestion(1, 10, 11), choiceQuestion(2, 20, 21)))

    @Test
    fun `semillas por plumas perdidas`() {
        assertEquals(100, calculatePoints(100, 0, 3, false, 0))
        assertEquals(75, calculatePoints(100, 1, 3, false, 0))
        assertEquals(50, calculatePoints(100, 2, 3, false, 0))
        assertEquals(0, calculatePoints(100, 3, 3, false, 0))
        assertEquals(0, calculatePoints(100, 5, 3, false, 0))
    }

    @Test
    fun `cada repaso paga la mitad del anterior hasta 1`() {
        assertEquals(listOf(50, 25, 13, 6, 3, 2, 1, 1, 1), (0..8).map { calculatePoints(100, 0, 3, true, it) })
        assertEquals(listOf(75, 38, 19), (0..2).map { calculatePoints(200, 1, 3, true, it) })
        assertEquals(0, calculatePoints(100, 3, 3, true, 9))
    }

    @Test
    fun `limite de tiempo con margen de 15 segundos`() {
        assertFalse(exceededTimeLimit(99999.0, null))
        assertFalse(exceededTimeLimit(45.0, 45))
        assertFalse(exceededTimeLimit(60.0, 45))
        assertTrue(exceededTimeLimit(60.5, 45))
        assertTrue(exceededTimeLimit(120.0, 45))
    }

    @Test
    fun `opcion multiple todo bien`() {
        val graded = gradeAnswers(
            twoChoices,
            listOf(AnswerSubmission(1, selectedOptionId = 10), AnswerSubmission(2, selectedOptionId = 20))
        )

        assertEquals(2, graded.correctAnswers)
        assertEquals(0, graded.wrongAnswers)
    }

    @Test
    fun `una pregunta sin respuesta cuenta como fallo`() {
        val graded = gradeAnswers(twoChoices, listOf(AnswerSubmission(1, selectedOptionId = 10)))

        assertEquals(1, graded.correctAnswers)
        assertEquals(1, graded.wrongAnswers)
    }

    @Test
    fun `si llegan dos respuestas para la misma pregunta gana la ultima`() {
        val graded = gradeAnswers(
            twoChoices,
            listOf(
                AnswerSubmission(1, selectedOptionId = 11),
                AnswerSubmission(1, selectedOptionId = 10),
                AnswerSubmission(2, selectedOptionId = 20)
            )
        )

        assertEquals(2, graded.correctAnswers)
        assertEquals(0, graded.wrongAnswers)
    }

    @Test
    fun `una opcion de otra pregunta no cuenta como acierto`() {
        val graded = gradeAnswers(
            twoChoices,
            listOf(AnswerSubmission(1, selectedOptionId = 20), AnswerSubmission(2, selectedOptionId = 20))
        )

        assertEquals(1, graded.correctAnswers)
        assertEquals(1, graded.wrongAnswers)
    }

    @Test
    fun `en relacion de conceptos cada par sin responder es un fallo`() {
        val graded = gradeAnswers(
            content(listOf(matchingQuestion(3, listOf(100, 101, 102)))),
            listOf(AnswerSubmission(3, pairId = 100, selectedPairId = 100))
        )

        assertEquals(1, graded.correctAnswers)
        assertEquals(2, graded.wrongAnswers)
    }

    @Test
    fun `en relacion de conceptos gana la ultima respuesta de cada par`() {
        val graded = gradeAnswers(
            content(listOf(matchingQuestion(3, listOf(100, 101, 102)))),
            listOf(
                AnswerSubmission(3, pairId = 100, selectedPairId = 999),
                AnswerSubmission(3, pairId = 100, selectedPairId = 100),
                AnswerSubmission(3, pairId = 101, selectedPairId = 101),
                AnswerSubmission(3, pairId = 102, selectedPairId = 102)
            )
        )

        assertEquals(3, graded.correctAnswers)
        assertEquals(0, graded.wrongAnswers)
    }

    @Test
    fun `respuestas de otra mision se ignoran`() {
        val graded = gradeAnswers(
            content(listOf(matchingQuestion(3, listOf(100, 101, 102)))),
            listOf(
                AnswerSubmission(999, pairId = 555, selectedPairId = 555),
                // El par 100 es de la pregunta 3, no de la 999.
                AnswerSubmission(999, pairId = 100, selectedPairId = 100)
            )
        )

        assertEquals(0, graded.correctAnswers)
        assertEquals(3, graded.wrongAnswers)
    }

    @Test
    fun `quedarse sin plumas pierde la mision y no paga`() {
        // 8 preguntas: 5 bien y 3 mal con 3 plumas.
        val questions = (1..8).map { choiceQuestion(it, it * 10, it * 10 + 1) }
        val answers = questions.mapIndexed { i, q ->
            AnswerSubmission(q.id, selectedOptionId = if (i < 5) q.id * 10 else q.id * 10 + 1)
        }

        val outcome = gradeLocally(content(questions), answers, timedOut = false, elapsedSeconds = 30.0,
            isReview = false, paidReviewsNearby = 0)

        assertEquals("failed", outcome.status)
        assertEquals(63, outcome.score)
        assertEquals(5, outcome.correctAnswers)
        assertEquals(3, outcome.wrongAnswers)
        assertEquals(0, outcome.plumasLeft)
        assertEquals(0, outcome.pointsEarned)
    }

    @Test
    fun `una mision completada con un error paga tres cuartos`() {
        val outcome = gradeLocally(
            twoChoices,
            listOf(AnswerSubmission(1, selectedOptionId = 10), AnswerSubmission(2, selectedOptionId = 21)),
            timedOut = false, elapsedSeconds = 30.0, isReview = false, paidReviewsNearby = 0
        )

        assertEquals("completed", outcome.status)
        assertEquals(50, outcome.score)
        assertEquals(2, outcome.plumasLeft)
        assertEquals(75, outcome.pointsEarned)
        assertFalse(outcome.isReview)
    }

    @Test
    fun `si la app aviso que se acabo el tiempo se pierde`() {
        val outcome = gradeLocally(twoChoices, perfect(), timedOut = true, elapsedSeconds = 10.0,
            isReview = false, paidReviewsNearby = 0)

        assertEquals("failed", outcome.status)
        assertEquals(0, outcome.pointsEarned)
    }

    @Test
    fun `pasarse del limite mas el margen se pierde aunque la app no lo diga`() {
        val outcome = gradeLocally(content(twoChoices.questions, timeLimitSeconds = 45), perfect(),
            timedOut = false, elapsedSeconds = 61.0, isReview = false, paidReviewsNearby = 0)

        assertEquals("failed", outcome.status)
        assertEquals(0, outcome.pointsEarned)
    }

    @Test
    fun `un repaso paga la mitad y el siguiente un cuarto`() {
        val first = gradeLocally(twoChoices, perfect(), timedOut = false, elapsedSeconds = 10.0,
            isReview = true, paidReviewsNearby = 0)
        val second = gradeLocally(twoChoices, perfect(), timedOut = false, elapsedSeconds = 10.0,
            isReview = true, paidReviewsNearby = 1)

        assertTrue(first.isReview)
        assertEquals(50, first.pointsEarned)
        assertEquals(25, second.pointsEarned)
    }

    @Test
    fun `sin maxPlumas usa las 3 de siempre`() {
        val outcome = gradeLocally(content(twoChoices.questions, maxPlumas = null), perfect(),
            timedOut = false, elapsedSeconds = 10.0, isReview = false, paidReviewsNearby = 0)

        assertEquals(3, outcome.plumasLeft)
        assertEquals(100, outcome.pointsEarned)
    }

    @Test
    fun `el tiempo jugado descuenta la pausa con tope de 5 minutos`() {
        assertEquals(120.0, playedSeconds(0, 120_000, 0), 0.0)
        assertEquals(30.0, playedSeconds(0, 120_000, 90), 0.0)
        assertEquals(300.0, playedSeconds(0, 600_000, 9999), 0.0)
        assertEquals(120.0, playedSeconds(0, 120_000, -50), 0.0)
    }

    @Test
    fun `el +30 s se cobra solo con reloj y saldo suficiente`() {
        assertEquals(ExtraTimePurchase(500, 30), extraTimePurchase(true, 45, 500))
        assertEquals(ExtraTimePurchase(0, 0), extraTimePurchase(true, 45, 499))
        assertEquals(ExtraTimePurchase(0, 0), extraTimePurchase(true, null, 9999))
        assertEquals(ExtraTimePurchase(0, 0), extraTimePurchase(false, 45, 9999))
    }

    @Test
    fun `los 30 s comprados se suman al limite`() {
        assertFalse(exceededTimeLimit(90.0, 45, extraSeconds = 30))
        assertTrue(exceededTimeLimit(90.5, 45, extraSeconds = 30))
    }

    @Test
    fun `con el tiempo extra 80 s en una mision de 45 s sigue a tiempo`() {
        val timed = content(twoChoices.questions, timeLimitSeconds = 45)

        val withExtra = gradeLocally(timed, perfect(), timedOut = false, elapsedSeconds = 80.0,
            isReview = false, paidReviewsNearby = 0, extraSeconds = 30)
        val withoutExtra = gradeLocally(timed, perfect(), timedOut = false, elapsedSeconds = 80.0,
            isReview = false, paidReviewsNearby = 0)

        assertEquals("completed", withExtra.status)
        assertEquals("failed", withoutExtra.status)
    }

    private fun perfect() = listOf(AnswerSubmission(1, selectedOptionId = 10), AnswerSubmission(2, selectedOptionId = 20))
}
