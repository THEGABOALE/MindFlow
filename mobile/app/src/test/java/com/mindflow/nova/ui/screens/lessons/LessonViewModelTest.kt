package com.mindflow.nova.ui.screens.lessons

import com.mindflow.nova.data.mission.AttemptStartResult
import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionOption
import com.mindflow.nova.data.model.MissionQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        id = 50, missionId = 3, score = 100, correctAnswers = 8, wrongAnswers = 0,
        plumasLeft = 3, pointsEarned = 200, isReview = false, status = "completed"
    )

    /** Repositorio falso: responde lo configurado y anota qué se le pidió. */
    private inner class FakeRepository : MissionRepository {
        var contentResult: (Int) -> MissionContentResult = { MissionContentResult.Loaded(content(it)) }
        var startResults = ArrayDeque<AttemptStartResult>()
        var finishResult: AttemptResult? = result
        var contentCalls = 0
        var startCalls = 0
        val finished = mutableListOf<Triple<Int, List<AnswerSubmission>, Boolean>>()
        private var nextAttemptId = 50

        override suspend fun loadContent(missionId: Int): MissionContentResult {
            contentCalls++
            return contentResult(missionId)
        }

        override suspend fun startAttempt(missionId: Int): AttemptStartResult {
            startCalls++
            return startResults.removeFirstOrNull() ?: AttemptStartResult.Started(nextAttemptId++)
        }

        override suspend fun finishAttempt(
            attemptId: Int,
            answers: List<AnswerSubmission>,
            timedOut: Boolean
        ): AttemptResult? {
            finished += Triple(attemptId, answers, timedOut)
            return finishResult
        }
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `antes de abrir esta cargando y no pidio nada`() {
        val repo = FakeRepository()
        val viewModel = LessonViewModel(repo)

        assertEquals(LessonState.Loading, viewModel.state.value)
        assertEquals(0, repo.contentCalls + repo.startCalls)
    }

    @Test
    fun `abrir carga el contenido y abre el primer intento`() {
        val viewModel = LessonViewModel(FakeRepository())

        viewModel.open(3)

        val state = viewModel.state.value as LessonState.Playing
        assertEquals(3, state.content.id)
        assertEquals(50, state.attemptId)
        assertEquals(1, state.attemptNumber)
    }

    @Test
    fun `abrir la misma mision dos veces no vuelve a pedir nada`() {
        val repo = FakeRepository()
        val viewModel = LessonViewModel(repo)

        viewModel.open(3)
        viewModel.open(3)

        assertEquals(1, repo.contentCalls)
        assertEquals(1, repo.startCalls)
    }

    @Test
    fun `si el contenido no carga queda el error y no se abre ningun intento`() {
        val repo = FakeRepository()
        repo.contentResult = { MissionContentResult.Failed("No se pudo cargar la misión (HTTP 404)") }
        val viewModel = LessonViewModel(repo)

        viewModel.open(3)

        assertEquals(LessonState.Error("No se pudo cargar la misión (HTTP 404)"), viewModel.state.value)
        assertEquals(0, repo.startCalls)
    }

    @Test
    fun `una mision sin preguntas avisa y no abre ningun intento`() {
        val repo = FakeRepository()
        repo.contentResult = { MissionContentResult.Loaded(content(it, questions = emptyList())) }
        val viewModel = LessonViewModel(repo)

        viewModel.open(5)

        assertEquals(LessonState.Error(EMPTY_MISSION_MESSAGE), viewModel.state.value)
        assertEquals(0, repo.startCalls)
    }

    @Test
    fun `si el intento no se puede abrir queda el error`() {
        val repo = FakeRepository()
        repo.startResults.add(AttemptStartResult.Failed("No se pudo iniciar el intento (HTTP 500)"))
        val viewModel = LessonViewModel(repo)

        viewModel.open(3)

        assertEquals(LessonState.Error("No se pudo iniciar el intento (HTTP 500)"), viewModel.state.value)
    }

    @Test
    fun `reintentar abre otro intento sin volver a pedir el contenido`() {
        val repo = FakeRepository()
        val viewModel = LessonViewModel(repo)
        viewModel.open(3)

        viewModel.retry()

        val state = viewModel.state.value as LessonState.Playing
        assertEquals(51, state.attemptId)
        assertEquals(2, state.attemptNumber)
        assertEquals(1, repo.contentCalls)
        assertEquals(2, repo.startCalls)
    }

    @Test
    fun `reintentar despues de un fallo al abrir el intento vuelve a probar`() {
        val repo = FakeRepository()
        repo.startResults.add(AttemptStartResult.Failed("sin red"))
        val viewModel = LessonViewModel(repo)
        viewModel.open(3)

        viewModel.retry()

        assertTrue(viewModel.state.value is LessonState.Playing)
    }

    @Test
    fun `reintentar sin contenido cargado no hace nada`() {
        val repo = FakeRepository()
        repo.contentResult = { MissionContentResult.Failed("x") }
        val viewModel = LessonViewModel(repo)
        viewModel.open(3)

        viewModel.retry()

        assertEquals(LessonState.Error("x"), viewModel.state.value)
        assertEquals(0, repo.startCalls)
    }

    @Test
    fun `cerrar manda las respuestas al intento en curso y devuelve el resultado`() = runTest {
        val repo = FakeRepository()
        val viewModel = LessonViewModel(repo)
        viewModel.open(3)
        viewModel.retry() // el intento en curso ahora es el 51
        val answers = listOf(AnswerSubmission(questionId = 1, selectedOptionId = 2))

        val outcome = viewModel.finishAttempt(answers, timedOut = true)

        assertEquals(result, outcome)
        assertEquals(listOf(Triple(51, answers, true)), repo.finished)
    }

    @Test
    fun `cerrar sin una leccion en curso devuelve null y no llama al backend`() = runTest {
        val repo = FakeRepository()
        val viewModel = LessonViewModel(repo)

        assertNull(viewModel.finishAttempt(emptyList(), timedOut = false))
        assertTrue(repo.finished.isEmpty())
    }

    @Test
    fun `si el backend no guarda el resultado, cerrar devuelve null`() = runTest {
        val repo = FakeRepository()
        repo.finishResult = null
        val viewModel = LessonViewModel(repo)
        viewModel.open(3)

        assertNull(viewModel.finishAttempt(emptyList(), timedOut = false))
    }
}
