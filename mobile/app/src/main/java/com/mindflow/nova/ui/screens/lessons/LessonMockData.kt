package com.mindflow.nova.ui.screens.lessons

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

/**
 * Contenido de la Lección 1 ("Bienvenida a NOVA"). El backend todavía no expone
 * un endpoint de preguntas, así que por ahora se usan datos locales que replican
 * el wireframe de Figma.
 */
object LessonMockData {

    val lessonOneMissionOrderIndex = 1

    val lessonOneQuestions = listOf(
        LessonQuestion(
            id = 1,
            prompt = "¿Qué significa igualdad?",
            options = listOf(
                LessonOption(1, "Primera respuesta", false, "Casi. La igualdad es que todas las personas tengan los mismos derechos, sin distinción."),
                LessonOption(2, "Segunda respuesta", true, "¡Exacto! La igualdad significa que todas las personas tienen los mismos derechos y oportunidades."),
                LessonOption(3, "Tercera respuesta", false, "No es así. La igualdad es que todas las personas tengan los mismos derechos, sin distinción."),
                LessonOption(4, "Cuarta respuesta", false, "No es así. La igualdad es que todas las personas tengan los mismos derechos, sin distinción.")
            )
        ),
        LessonQuestion(
            id = 2,
            prompt = "¿Qué es la dignidad?",
            options = listOf(
                LessonOption(1, "Primera respuesta", false, "No era esa. La dignidad es el valor y respeto que merece cada persona."),
                LessonOption(2, "Segunda respuesta", true, "¡Bien! La dignidad es el respeto que merece toda persona por el simple hecho de serlo."),
                LessonOption(3, "Tercera respuesta", false, "No era esa. La dignidad es el valor y respeto que merece cada persona."),
                LessonOption(4, "Cuarta respuesta", false, "No era esa. La dignidad es el valor y respeto que merece cada persona.")
            )
        ),
        LessonQuestion(
            id = 3,
            prompt = "¿Cuáles son los derechos de la mujer?",
            options = listOf(
                LessonOption(1, "Primera respuesta", false, "Recordá: los derechos de la mujer incluyen educación, salud y una vida libre de violencia."),
                LessonOption(2, "Segunda respuesta", true, "¡Correcto! Los derechos de la mujer incluyen educación, salud y una vida libre de violencia."),
                LessonOption(3, "Tercera respuesta", false, "Recordá: los derechos de la mujer incluyen educación, salud y una vida libre de violencia."),
                LessonOption(4, "Cuarta respuesta", false, "Recordá: los derechos de la mujer incluyen educación, salud y una vida libre de violencia.")
            )
        ),
        LessonQuestion(
            id = 4,
            prompt = "¿Qué es la equidad de género?",
            options = listOf(
                LessonOption(1, "Primera respuesta", false, "Fijate bien: la equidad de género es dar a cada quien lo que necesita para tener las mismas oportunidades."),
                LessonOption(2, "Segunda respuesta", true, "¡Genial! La equidad de género es dar a cada quien lo que necesita para tener las mismas oportunidades."),
                LessonOption(3, "Tercera respuesta", false, "Fijate bien: la equidad de género es dar a cada quien lo que necesita para tener las mismas oportunidades."),
                LessonOption(4, "Cuarta respuesta", false, "Fijate bien: la equidad de género es dar a cada quien lo que necesita para tener las mismas oportunidades.")
            )
        ),
        LessonQuestion(
            id = 5,
            prompt = "¿Qué significa empoderamiento?",
            options = listOf(
                LessonOption(1, "Primera respuesta", false, "Empoderamiento es que las personas puedan tomar decisiones sobre su propia vida. Seguí practicando."),
                LessonOption(2, "Segunda respuesta", true, "¡Perfecto! Empoderamiento es que las personas puedan tomar decisiones sobre su propia vida."),
                LessonOption(3, "Tercera respuesta", false, "Empoderamiento es que las personas puedan tomar decisiones sobre su propia vida. Seguí practicando."),
                LessonOption(4, "Cuarta respuesta", false, "Empoderamiento es que las personas puedan tomar decisiones sobre su propia vida. Seguí practicando.")
            )
        )
    )
}
