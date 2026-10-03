package com.mindflow.nova.ui.screens.lessons

import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerRevealTest {

    @Test
    fun `opcion multiple - la correcta siempre se marca y la elegida mal tambien`() {
        assertEquals(AnswerReveal.CORRECT, answerReveal(isCorrect = true, isChosen = true))
        assertEquals(AnswerReveal.CORRECT, answerReveal(isCorrect = true, isChosen = false))
        assertEquals(AnswerReveal.WRONG_CHOSEN, answerReveal(isCorrect = false, isChosen = true))
        assertEquals(AnswerReveal.NEUTRAL, answerReveal(isCorrect = false, isChosen = false))
    }

    @Test
    fun `verdadero o falso - mientras responde solo se ve lo elegido`() {
        assertEquals(TrueFalseButtonState.SELECTED, truthButtonState(answered = false, thisValue = true, selected = true, correctAnswer = false))
        assertEquals(TrueFalseButtonState.IDLE, truthButtonState(answered = false, thisValue = false, selected = true, correctAnswer = false))
    }

    @Test
    fun `verdadero o falso - al fallar se ve la elegida en rojo y la correcta en verde`() {
        // Eligió VERDADERO y era FALSO.
        assertEquals(TrueFalseButtonState.WRONG, truthButtonState(answered = true, thisValue = true, selected = true, correctAnswer = false))
        assertEquals(TrueFalseButtonState.CORRECT, truthButtonState(answered = true, thisValue = false, selected = true, correctAnswer = false))
    }

    @Test
    fun `verdadero o falso - al acertar solo se marca la elegida`() {
        assertEquals(TrueFalseButtonState.CORRECT, truthButtonState(answered = true, thisValue = true, selected = true, correctAnswer = true))
        assertEquals(TrueFalseButtonState.IDLE, truthButtonState(answered = true, thisValue = false, selected = true, correctAnswer = true))
    }
}
