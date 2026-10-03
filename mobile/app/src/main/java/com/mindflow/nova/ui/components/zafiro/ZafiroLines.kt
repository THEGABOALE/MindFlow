package com.mindflow.nova.ui.components.zafiro

/**
 * Lo que dice Zafiro en cada momento. Son frases de borrador: el equipo las
 * ajusta acá, en un solo lugar, cuando defina la personalidad de Zafiro.
 */
object ZafiroLines {
    const val WELCOME = "¡Hola! Soy Zafiro y te voy a acompañar en cada misión."
    const val ACCESS_CODE = "Pídele el código a tu docente y escríbelo aquí."
    const val HOME = "¡Qué bueno verte! ¿Seguimos aprendiendo?"
    const val START_MISSION = "¡Vamos! Lee con calma, yo te acompaño."
    const val START_REVIEW = "Repasar también suma. ¡A practicar!"
    const val CORRECT = "¡Muy bien!"
    const val WRONG = "Casi. Mira por qué:"
    const val MATCHING_PLAYING = "¡Vas bien! Elige un par."
    const val MATCHING_WRONG = "Casi, ese no era. ¡Tú puedes!"
    const val OUT_OF_PLUMAS = "Se acabaron las plumas, pero lo que aprendiste se queda contigo. ¿Lo intentamos de nuevo?"
    const val TIME_UP = "¡Se acabó el tiempo! La próxima vez puedes pausar o pedir más tiempo."
    const val COMPLETED = "¡Lo lograste! Cada misión te hace más fuerte."

    val all = listOf(
        WELCOME, ACCESS_CODE, HOME, START_MISSION, START_REVIEW, CORRECT, WRONG,
        MATCHING_PLAYING, MATCHING_WRONG, OUT_OF_PLUMAS, TIME_UP, COMPLETED
    )

    /** Lo que dice al responder: felicita o anima, y después la explicación de la pregunta. */
    fun feedback(isCorrect: Boolean, explanation: String): String {
        val text = explanation.trim()
        return when {
            isCorrect && text.isEmpty() -> CORRECT
            isCorrect -> "$CORRECT $text"
            text.isEmpty() -> "Casi."
            else -> "$WRONG $text"
        }
    }
}
