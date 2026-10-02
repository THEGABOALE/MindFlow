package com.mindflow.nova.data.offline

import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.MissionContent

// Copia en Kotlin de backend/src/services/mission-grading.service.js, para dar
// el resultado al terminar una misión sin conexión. El servidor la vuelve a
// calificar al subirla y su resultado es el que vale; si estas reglas cambian
// allá, hay que cambiarlas acá también.
//
// Los redondeos usan Math.round de Java, que como el de JavaScript sube los .5
// (kotlin.math.round los lleva al par: 12.5 daría 12 en vez de 13).

private const val REVIEW_FACTOR = 0.5
private const val MIN_REVIEW_POINTS = 1
private const val TIME_LIMIT_GRACE_SECONDS = 15
private const val DEFAULT_MAX_PLUMAS = 3

/** Cada repaso cobrado de la misma misión dentro de esta ventana parte a la mitad lo que paga el siguiente. */
const val REVIEW_PAY_WINDOW_MS = 24L * 60 * 60 * 1000

/**
 * Semillas de una misión: cada pluma perdida descuenta 1/(maxPlumas + 1) y sin
 * plumas no paga. Un repaso paga la mitad, y cada repaso cobrado cerca
 * ([paidReviewsNearby]) la vuelve a partir, hasta un mínimo de 1.
 */
fun calculatePoints(
    pointsReward: Int,
    wrongAnswers: Int,
    maxPlumas: Int,
    isReview: Boolean,
    paidReviewsNearby: Int
): Int {
    if (maxPlumas - wrongAnswers <= 0) return 0

    val earned = pointsReward * (1 - wrongAnswers.toDouble() / (maxPlumas + 1))

    if (!isReview) return Math.round(earned).toInt()

    val reviewPoints = earned * Math.pow(REVIEW_FACTOR, (paidReviewsNearby + 1).toDouble())

    return maxOf(MIN_REVIEW_POINTS, Math.round(reviewPoints).toInt())
}

/** Igual que en el servidor: el límite más 15 s de margen. */
fun exceededTimeLimit(elapsedSeconds: Double, timeLimitSeconds: Int?): Boolean =
    timeLimitSeconds != null && elapsedSeconds > timeLimitSeconds + TIME_LIMIT_GRACE_SECONDS

data class GradedAnswers(val correctAnswers: Int, val wrongAnswers: Int)

/**
 * Corrige contra las preguntas de la misión, no contra lo que llegó: una
 * pregunta o par sin respuesta es un fallo, lo que no es de esta misión se
 * ignora y si hay varias respuestas para lo mismo gana la última.
 */
fun gradeAnswers(content: MissionContent, answers: List<AnswerSubmission>): GradedAnswers {
    // Opción -> pregunta a la que pertenece, y par -> pregunta.
    val optionQuestion = content.questions
        .flatMap { q -> q.options.map { it.id to (q.id to it.isCorrect) } }
        .toMap()
    val pairQuestion = content.questions.flatMap { q -> q.pairs.map { it.id to q.id } }.toMap()

    val answerByQuestion = mutableMapOf<Int, AnswerSubmission>()
    val answerByPair = mutableMapOf<Int, AnswerSubmission>()

    for (answer in answers) {
        if (answer.pairId != null) {
            if (pairQuestion[answer.pairId] == answer.questionId) answerByPair[answer.pairId] = answer
        } else if (answer.selectedOptionId != null) {
            answerByQuestion[answer.questionId] = answer
        }
    }

    var correct = 0
    var wrong = 0

    for (question in content.questions) {
        if (question.type == "matching") {
            for (pair in question.pairs) {
                if (answerByPair[pair.id]?.selectedPairId == pair.id) correct++ else wrong++
            }
            continue
        }

        val option = answerByQuestion[question.id]?.selectedOptionId?.let { optionQuestion[it] }
        val isCorrect = option != null && option.first == question.id && option.second

        if (isCorrect) correct++ else wrong++
    }

    return GradedAnswers(correct, wrong)
}

/** Resultado calculado en el teléfono; los campos son los mismos que devuelve el servidor. */
data class LocalOutcome(
    val status: String,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val plumasLeft: Int,
    val pointsEarned: Int,
    val isReview: Boolean
)

fun gradeLocally(
    content: MissionContent,
    answers: List<AnswerSubmission>,
    timedOut: Boolean,
    elapsedSeconds: Double,
    isReview: Boolean,
    paidReviewsNearby: Int
): LocalOutcome {
    val maxPlumas = content.maxPlumas ?: DEFAULT_MAX_PLUMAS
    val (correct, wrong) = gradeAnswers(content, answers)

    val ranOutOfTime = timedOut || exceededTimeLimit(elapsedSeconds, content.timeLimitSeconds)
    val failed = ranOutOfTime || wrong >= maxPlumas
    val total = correct + wrong

    return LocalOutcome(
        status = if (failed) "failed" else "completed",
        score = if (total > 0) Math.round(correct.toDouble() / total * 100).toInt() else 0,
        correctAnswers = correct,
        wrongAnswers = wrong,
        plumasLeft = maxOf(maxPlumas - wrong, 0),
        pointsEarned = if (failed) 0 else calculatePoints(content.pointsReward, wrong, maxPlumas, isReview, paidReviewsNearby),
        isReview = isReview
    )
}
