package com.mindflow.nova.ui.screens.home

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.student.LevelsResult
import com.mindflow.nova.data.student.StudentRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentHomeViewModelTest {

    private val level = LevelResponse(
        id = 1, name = "Primaria alta", code = "primaria_alta", description = null, orderIndex = 1, missions = emptyList()
    )

    private fun user(id: Int) = SessionUser(
        id = id, fullName = "Estudiante $id", email = null, loginId = "e$id", role = "student", centerId = 1, group = null
    )

    private fun progress(userId: Int, points: Int) = StudentProgress(
        id = userId, fullName = "Estudiante $userId", totalPoints = points, missionsCompleted = 0, levels = emptyList()
    )

    /** Repositorio falso: devuelve lo que se le configure y cuenta cuántas veces se le pidió cada cosa. */
    private class FakeRepository(
        var levels: LevelsResult,
        var user: SessionUser?,
        var progress: StudentProgress?
    ) : StudentRepository {
        var levelCalls = 0
        var userCalls = 0
        var progressCalls = 0
        var progressGate: CompletableDeferred<Unit>? = null
        /** Respuestas por llamada, en orden, cada una con su propia espera; si está vacía se usa [progress]. */
        val progressQueue = ArrayDeque<Pair<StudentProgress?, CompletableDeferred<Unit>>>()

        override suspend fun loadLevels(): LevelsResult { levelCalls++; return levels }
        override suspend fun loadCurrentUser(): SessionUser? { userCalls++; return user }
        override suspend fun loadProgress(userId: Int): StudentProgress? {
            progressCalls++
            progressQueue.removeFirstOrNull()?.let { (answer, gate) ->
                gate.await()
                return answer
            }
            val answer = progress
            progressGate?.await()
            return answer
        }
    }

    private fun repository(
        levels: LevelsResult = LevelsResult.Loaded(listOf(level)),
        user: SessionUser? = user(2),
        progress: StudentProgress? = progress(2, 250)
    ) = FakeRepository(levels, user, progress)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `antes de empezar esta cargando y no pidio nada`() {
        val repo = repository()
        val viewModel = StudentHomeViewModel(repo)

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(0, repo.levelCalls + repo.userCalls + repo.progressCalls)
    }

    @Test
    fun `al empezar carga la ruta, la cuenta y el progreso`() {
        val viewModel = StudentHomeViewModel(repository())

        viewModel.start()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf(level), state.levels)
        assertEquals(2, state.user?.id)
        assertEquals(250, state.progress?.totalPoints)
    }

    @Test
    fun `empezar dos veces no vuelve a pedir nada`() {
        val repo = repository()
        val viewModel = StudentHomeViewModel(repo)

        viewModel.start()
        viewModel.start()

        assertEquals(1, repo.levelCalls)
        assertEquals(1, repo.userCalls)
        assertEquals(1, repo.progressCalls)
    }

    @Test
    fun `si la ruta no carga queda el mensaje de error y deja de cargar`() {
        val viewModel = StudentHomeViewModel(repository(levels = LevelsResult.Failed("Error HTTP: 500")))

        viewModel.start()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Error HTTP: 500", viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.levels.isEmpty())
    }

    @Test
    fun `refrescar pide el progreso de nuevo sin volver a pedir la cuenta`() {
        val repo = repository()
        val viewModel = StudentHomeViewModel(repo)
        viewModel.start()

        repo.progress = progress(2, 450)
        viewModel.refreshProgress()

        assertEquals(450, viewModel.state.value.progress?.totalPoints)
        assertEquals(1, repo.userCalls)
        assertEquals(2, repo.progressCalls)
    }

    @Test
    fun `si refrescar falla se conserva el progreso anterior`() {
        val repo = repository()
        val viewModel = StudentHomeViewModel(repo)
        viewModel.start()

        repo.progress = null
        viewModel.refreshProgress()

        assertEquals(250, viewModel.state.value.progress?.totalPoints)
    }

    @Test
    fun `si la cuenta no carga al inicio, refrescar la vuelve a pedir`() {
        val repo = repository(user = null)
        val viewModel = StudentHomeViewModel(repo)
        viewModel.start()
        assertNull(viewModel.state.value.user)
        assertEquals(0, repo.progressCalls)

        repo.user = user(2)
        viewModel.refreshProgress()

        assertEquals(2, viewModel.state.value.user?.id)
        assertEquals(250, viewModel.state.value.progress?.totalPoints)
    }

    @Test
    fun `al cerrar sesion se olvida todo y la siguiente cuenta carga sus propios datos`() {
        val repo = repository()
        val viewModel = StudentHomeViewModel(repo)
        viewModel.start()

        viewModel.clear()

        assertEquals(StudentHomeState(), viewModel.state.value)

        repo.user = user(7)
        repo.progress = progress(7, 10)
        viewModel.start()

        assertEquals(7, viewModel.state.value.user?.id)
        assertEquals(10, viewModel.state.value.progress?.totalPoints)
        assertEquals(2, repo.levelCalls)
    }

    @Test
    fun `una respuesta que llega despues de cerrar sesion no deja datos de la cuenta anterior`() {
        val repo = repository()
        val gate = CompletableDeferred<Unit>()
        repo.progressGate = gate
        val viewModel = StudentHomeViewModel(repo)
        viewModel.start() // el progreso queda esperando la respuesta

        viewModel.clear()
        gate.complete(Unit) // la respuesta vieja llega tarde

        assertNull(viewModel.state.value.progress)
        assertNull(viewModel.state.value.user)
    }

    @Test
    fun `una actualizacion vieja que llega tarde no pisa a la nueva`() {
        val repo = repository()
        val slowOld = CompletableDeferred<Unit>()
        val fastNew = CompletableDeferred<Unit>().apply { complete(Unit) }
        repo.progressQueue.add(progress(2, 250) to slowOld) // la del inicio, lenta
        repo.progressQueue.add(progress(2, 450) to fastNew) // la de salir de una lección, rápida
        val viewModel = StudentHomeViewModel(repo)

        viewModel.start()
        viewModel.refreshProgress()
        assertEquals(450, viewModel.state.value.progress?.totalPoints)

        slowOld.complete(Unit) // la respuesta vieja llega después

        assertEquals(450, viewModel.state.value.progress?.totalPoints)
    }
}
