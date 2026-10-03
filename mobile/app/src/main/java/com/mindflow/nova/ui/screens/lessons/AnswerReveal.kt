package com.mindflow.nova.ui.screens.lessons

// Cómo se muestra cada opción después de responder. El acierto y el error se
// marcan con color, ícono y texto a la vez: el color nunca es la única señal.

/** Opción múltiple: la correcta siempre se marca; la elegida mal, también. */
internal enum class AnswerReveal { CORRECT, WRONG_CHOSEN, NEUTRAL }

internal fun answerReveal(isCorrect: Boolean, isChosen: Boolean): AnswerReveal = when {
    isCorrect -> AnswerReveal.CORRECT
    isChosen -> AnswerReveal.WRONG_CHOSEN
    else -> AnswerReveal.NEUTRAL
}

internal enum class TrueFalseButtonState { IDLE, SELECTED, CORRECT, WRONG }

/**
 * Verdadero/falso: mientras responde solo se ve lo elegido. Después, la
 * elegida queda en verde o en rojo y, si falló, la correcta aparece en verde.
 */
internal fun truthButtonState(
    answered: Boolean,
    thisValue: Boolean,
    selected: Boolean?,
    correctAnswer: Boolean
): TrueFalseButtonState = when {
    !answered -> if (selected == thisValue) TrueFalseButtonState.SELECTED else TrueFalseButtonState.IDLE
    thisValue == correctAnswer -> TrueFalseButtonState.CORRECT
    selected == thisValue -> TrueFalseButtonState.WRONG
    else -> TrueFalseButtonState.IDLE
}
