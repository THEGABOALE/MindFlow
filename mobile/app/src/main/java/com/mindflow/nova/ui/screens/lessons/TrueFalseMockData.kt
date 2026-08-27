package com.mindflow.nova.ui.screens.lessons

data class TrueFalseQuestion(
    val id: Int,
    val statement: String,
    val correctAnswer: Boolean,
    val explanation: String
)

/**
 * Cuestionario de verdadero/falso para la misión "Decisiones con respeto".
 * Transcrito del wireframe de Figma (página Playground): las 8 afirmaciones
 * y explicaciones que dibujaste ahí, tal cual.
 */
object TrueFalseMockData {

    val decisionesConRespetoQuestions = listOf(
        TrueFalseQuestion(
            id = 1,
            statement = "La equidad de género en el hogar significa que las niñas deben encargarse prioritariamente de las tareas domésticas, mientras que los niños deben enfocarse únicamente en sus estudios.",
            correctAnswer = false,
            explanation = "Las cartillas del MINED enfatizan que la educación en valores promueve la igualdad y la corresponsabilidad. Tanto niños como niñas tienen los mismos derechos a estudiar y la misma responsabilidad de participar en las tareas de la casa."
        ),
        TrueFalseQuestion(
            id = 2,
            statement = "Los celos extremos, la revisión del teléfono móvil y el control sobre la ropa de la pareja son manifestaciones de afecto y no se consideran formas de violencia.",
            correctAnswer = false,
            explanation = "La cartilla identifica el control obsesivo y el aislamiento como signos claros de violencia psicológica y emocional. Estas conductas vulneran la autonomía de la mujer y constituyen señales de alerta temprana ante posibles agresiones físicas."
        ),
        TrueFalseQuestion(
            id = 3,
            statement = "Cualquier mujer o familiar que identifique una situación de riesgo por violencia de género puede solicitar ayuda y realizar la denuncia a través de la Comisaría de la Mujer o llamando a la línea 118.",
            correctAnswer = true,
            explanation = "La Policía Nacional, mediante las Comisarías de la Mujer y la línea de emergencia 118, forma parte de la red de respuesta institucional inmediata para garantizar la protección integral y la atención a las víctimas."
        ),
        TrueFalseQuestion(
            id = 4,
            statement = "Las Consejerías de las Comunidades Educativas tienen como objetivo principal aplicar medidas disciplinarias y sancionar con expulsión a las estudiantes afectadas por problemas familiares.",
            correctAnswer = false,
            explanation = "La función de las Consejerías Educativas es estrictamente preventiva, de acompañamiento, escucha activa y detección temprana de situaciones de vulnerabilidad pedagógica y emocional para brindar apoyo a la comunidad estudiantil."
        ),
        TrueFalseQuestion(
            id = 5,
            statement = "Toda mujer tiene derecho a tomar sus propias decisiones en cuanto a su educación, trabajo y proyecto de vida sin requerir la aprobación o permiso de su pareja.",
            correctAnswer = true,
            explanation = "Las cartillas promueven el principio de dignidad e independencia individual, resaltando que la mujer es un sujeto pleno de derechos con capacidad y libertad de desarrollarse en cualquier ámbito personal, académico o laboral."
        ),
        TrueFalseQuestion(
            id = 6,
            statement = "El femicidio es una forma extrema de violencia que puede prevenirse si se identifican y denuncian a tiempo las agresiones psicológicas, verbales y físicas.",
            correctAnswer = true,
            explanation = "Las cartillas destacan que la violencia suele escalar. Identificar conductas tempranas como amenazas, insultos o chantajes permite activar los mecanismos de protección antes de que ocurra un desenlace fatal."
        ),
        TrueFalseQuestion(
            id = 7,
            statement = "La responsabilidad del cuidado, alimentación y desarrollo emocional de las hijas e hijos recae de forma exclusiva en la madre.",
            correctAnswer = false,
            explanation = "Las cartillas promueven una paternidad responsable donde ambos progenitores asumen de manera equitativa la crianza, el afecto y los cuidados necesarios para el desarrollo integral de los hijos."
        ),
        TrueFalseQuestion(
            id = 8,
            statement = "Los derechos de las mujeres solo deben respetarse dentro del entorno familiar y pierden vigencia en los espacios de trabajo, escuelas o comunidades.",
            correctAnswer = false,
            explanation = "Los derechos humanos de las mujeres son universales e inalienables. Las cartillas remarcan que deben respetarse y garantizarse en todos los espacios sin excepción: el hogar, la escuela, el centro laboral y la comunidad."
        )
    )
}
