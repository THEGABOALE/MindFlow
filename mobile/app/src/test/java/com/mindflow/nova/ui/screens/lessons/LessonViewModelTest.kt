package com.mindflow.nova.ui.screens.lessons

import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionOption
import com.mindflow.nova.data.model.MissionQuestion
import com.mindflow.nova.data.offline.AttemptRecorder
import com.mindflow.nova.data.offline.FinishedAttempt
import com.mindflow.nova.data.offline.LessonOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

    private val question = MissionQuestion(
        id = 1, prompt = "¿Verdadero?", type = "true_false", feedback = null, orderIndex = 1, points = 10,
        options = listOf(MissionOption(id = 1, text = "Verdadero", isCorrect = true, feedback = null, orderIndex = 1))
    )

    private fun content(id: Int, questions: List<MissionQuestion> = listOf(question)) = MissionContent(
        id = id, levelId = 1, title = "Misión $id", description = null, topic = null, orderIndex = id,
        pointsReward = 100, mechanic = "true_false", timeLimitSeconds = null, maxPlumas = 3, questions = questions
    )

    private val result = AttemptResult(
        id = 0, missionId = 3, score = 100, correctAnswers = 8, wrongAnswers = 0,
        plumasLeft = 3, pointsEarned = 200, isReview = false, status = "completed"
    )

    /** Repositorio falso: responde lo configurado y cuenta cuántas veces se le pidió contenido. */
    private class FakeRepository(var contentResult: (Int) -> MissionContentResult) : MissionRepository {
        var contentCalls = 0

        override suspend fun loadContent(missionId: Int): MissionContentResult {
            contentCalls++
            return contentResult(missionId)
        }

        override suspend fun prefetch(missionIds: List<Int>) = Unit
    }

    private class FakeRecorder(var outcome: LessonOutcome) : AttemptRecorder {
        val recorded = mutableListOf<FinishedAttempt>()

        override suspend fun record(attempt: FinishedAttempt): LessonOutcome {
            recorded += attempt
            return outcome
        }
    }

    private val repo = FakeRepository { MissionContentResult.Loaded(content(it)) }
    private val recorder = FakeRecorder(LessonOutcome.Synced(result))
    private var clock = 1_000_000L
    private var nextId = 0

    private fun viewModel() = LessonViewModel(
        repository = repo,
        recorder = recorder,
        now = { clock },
        newId = { "intento-${++nextId}" },
        tzOffset = { -360 }
    )

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `antes de abrir esta cargando y no pidio nada`() {
        val viewModel = viewModel()

        assertEquals(LessonState.Loading, viewModel.state.value)
        assertEquals(0, repo.contentCalls)
    }

    @Test
    fun `abrir carga el contenido y crea el intento en el telefono`() {
        val viewModel = viewModel()

        viewModel.open(3)

        assertEquals(LessonState.Playing(content(3), "intento-1", 1), viewModel.state.value)
    }

    @Test
    fun `abrir la misma mision dos veces no vuelve a pedir nada`() {
        val viewModel = viewModel()

        viewModel.open(3)
        viewModel.open(3)

        assertEquals(1, repo.contentCalls)
        assertEquals(1, nextId)
    }

    @Test
    fun `si el contenido no carga queda el error`() {
        repo.contentResult = { MissionContentResult.Failed("Esta misión todavía no está descargada.") }
        val viewModel = viewModel()

        viewModel.open(3)

        assertEquals(LessonState.Error("Esta misión todavía no está descargada."), viewModel.state.value)
    }

    @Test
    fun `una mision sin preguntas avisa y no crea ningun intento`() {
        repo.contentResult = { MissionContentResult.Loaded(content(it, questions = emptyList())) }
        val viewModel = viewModel()

        viewModel.open(5)

        assertEquals(LessonState.Error(EMPTY_MISSION_MESSAGE), viewModel.state.value)
        assertEquals(0, nextId)
    }

    @Test
    fun `reintentar crea otro intento sin volver a pedir el contenido`() {
        val viewModel = viewModel()
        viewModel.open(3)

        viewModel.retry()

        assertEquals(LessonState.Playing(content(3), "intento-2", 2), viewModel.state.value)
        assertEquals(1, repo.contentCalls)
    }

    @Test
    fun `reintentar sin contenido cargado no hace nada`() {
        repo.contentResult = { MissionContentResult.Failed("x") }
        val viewModel = viewModel()
        viewModel.open(3)

        viewModel.retry()

        assertEquals(LessonState.Error("x"), viewModel.state.value)
    }

    @Test
    fun `terminar entrega al recorder el intento en curso con sus horas`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        clock += 5_000
        viewModel.retry() // el intento en curso ahora es el 2, empezado en este momento
        val startedRetry = clock
        clock += 42_000
        val answers = listOf(AnswerSubmission(questionId = 1, selectedOptionId = 1))

        val outcome = viewModel.finishAttempt(answers, timedOut = true)

        assertEquals(LessonOutcome.Synced(result), outcome)
        assertEquals(
            listOf(FinishedAttempt("intento-2", content(3), answers, true, startedRetry, clock, -360)),
            recorder.recorded
        )
    }

    @Test
    fun `la pausa del reloj se descuenta del intento`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        clock += 10_000
        viewModel.pauseClock()
        clock += 60_000
        viewModel.resumeClock()
        clock += 30_000

        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(60, recorder.recorded.single().pausedSeconds)
        assertFalse(recorder.recorded.single().usedExtraTime)
    }

    @Test
    fun `pausar dos veces seguidas no cuenta doble`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.pauseClock()
        clock += 20_000
        viewModel.pauseClock()
        clock += 20_000
        viewModel.resumeClock()
        viewModel.resumeClock()

        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(40, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `terminar en pausa cierra la pausa con la hora de fin`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.pauseClock()
        clock += 15_500

        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(15, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `comprar +30 s queda en el intento`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)

        viewModel.buyExtraTime()
        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertTrue(recorder.recorded.single().usedExtraTime)
    }

    @Test
    fun `reintentar empieza sin pausa ni potenciador`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.buyExtraTime()
        viewModel.pauseClock()
        clock += 30_000
        viewModel.resumeClock()

        viewModel.retry()
        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(0, recorder.recorded.single().pausedSeconds)
        assertFalse(recorder.recorded.single().usedExtraTime)
    }

    @Test
    fun `terminar devuelve lo que diga el recorder`() = runTest {
        recorder.outcome = LessonOutcome.Rejected("Primero completa la misión anterior")
        val viewModel = viewModel()
        viewModel.open(3)

        assertEquals(LessonOutcome.Rejected("Primero completa la misión anterior"), viewModel.finishAttempt(emptyList(), false))
    }

    @Test
    fun `terminar sin una leccion en curso no guarda nada`() = runTest {
        val viewModel = viewModel()

        assertEquals(LessonOutcome.SaveFailed, viewModel.finishAttempt(emptyList(), timedOut = false))
        assertTrue(recorder.recorded.isEmpty())
    }
}
