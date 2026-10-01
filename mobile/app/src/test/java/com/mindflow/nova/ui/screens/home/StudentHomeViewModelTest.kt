package com.mindflow.nova.ui.screens.home

import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.model.SessionGroup
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.RejectedNotice
import com.mindflow.nova.data.student.LevelsResult
import com.mindflow.nova.data.student.StudentRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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

    private fun mission(id: Int, published: Boolean = true) = MissionResponse(
        id = id, title = "Misión $id", description = null, topic = null, orderIndex = id, pointsReward = 100,
        mechanic = "multiple_choice", timeLimitSeconds = null, maxPlumas = 3, isPublished = published
    )

    private val level = LevelResponse(
        id = 1, name = "Primaria alta", code = "primaria_alta", description = null, orderIndex = 1,
        missions = listOf(mission(10), mission(11), mission(12, published = false))
    )
    private val otherLevel = LevelResponse(
        id = 2, name = "Secundaria", code = "secundaria", description = null, orderIndex = 2,
        missions = listOf(mission(20))
    )

    private fun user(id: Int) = SessionUser(
        id = id, fullName = "Estudiante $id", email = null, loginId = "e$id", role = "student", centerId = 1,
        group = SessionGroup(id = 1, name = "5A", grade = "5", section = "A", schoolYear = 2026, levelId = 1)
    )

    private fun progress(userId: Int, points: Int) = StudentProgress(
        id = userId, fullName = "Estudiante $userId", totalPoints = points, missionsCompleted = 0, levels = emptyList()
    )

    /**
     * Repositorio falso que se comporta como el real: lo que trae [loadProgress]
     * queda guardado y [observeProgress] lo emite. Cuenta cuántas veces se le
     * pidió cada cosa.
     */
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
        /** Lo "guardado en el teléfono", por usuario. */
        val saved = MutableStateFlow<Map<Int, StudentProgress>>(emptyMap())
        val pendingCount = MutableStateFlow(0)

        override suspend fun loadLevels(): LevelsResult { levelCalls++; return levels }
        override suspend fun loadCurrentUser(): SessionUser? { userCalls++; return user }
        override fun observeProgress(userId: Int): Flow<StudentProgress?> = saved.map { it[userId] }
        override fun observePendingCount(userId: Int): Flow<Int> = pendingCount

        override suspend fun loadProgress(userId: Int): StudentProgress? {
            progressCalls++
            val answer = progressQueue.removeFirstOrNull()?.let { (answer, gate) ->
                gate.await()
                answer
            } ?: run {
                val answer = progress
                progressGate?.await()
                answer
            }
            answer?.let { fresh -> saved.update { it + (userId to fresh) } }
            return answer
        }
    }

    private class FakeMissions : MissionRepository {
        val prefetched = mutableListOf<List<Int>>()

        override suspend fun loadContent(missionId: Int): MissionContentResult = error("no se usa")
        override suspend fun prefetch(missionIds: List<Int>) { prefetched += missionIds }
    }

    private val notices = MutableStateFlow<List<RejectedNotice>>(emptyList())
    private val missions = FakeMissions()

    private fun repository(
        levels: LevelsResult = LevelsResult.Loaded(listOf(otherLevel, level)),
        user: SessionUser? = user(2),
        progress: StudentProgress? = progress(2, 250)
    ) = FakeRepository(levels, user, progress)

    private fun viewModel(repo: FakeRepository) = StudentHomeViewModel(
        repository = repo,
        missions = missions,
        rejectedNotices = notices,
        onNoticesSeen = { notices.value = emptyList() }
    )

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `antes de empezar esta cargando y no pidio nada`() {
        val repo = repository()
        val viewModel = viewModel(repo)

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(0, repo.levelCalls + repo.userCalls + repo.progressCalls)
    }

    @Test
    fun `al empezar carga la ruta, la cuenta y el progreso`() {
        val viewModel = viewModel(repository())

        viewModel.start()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf(otherLevel, level), state.levels)
        assertEquals(2, state.user?.id)
        assertEquals(250, state.progress?.totalPoints)
        assertFalse(state.offline)
    }

    @Test
    fun `empezar dos veces no vuelve a pedir nada`() {
        val repo = repository()
        val viewModel = viewModel(repo)

        viewModel.start()
        viewModel.start()

        assertEquals(1, repo.levelCalls)
        assertEquals(1, repo.userCalls)
        assertEquals(1, repo.progressCalls)
    }

    @Test
    fun `si la ruta no carga queda el mensaje de error y deja de cargar`() {
        val viewModel = viewModel(repository(levels = LevelsResult.Failed("Error HTTP: 500")))

        viewModel.start()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Error HTTP: 500", viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.levels.isEmpty())
    }

    @Test
    fun `el progreso se actualiza solo cuando cambia lo guardado`() {
        val repo = repository()
        val viewModel = viewModel(repo)
        viewModel.start()

        // Por ejemplo, terminó una subida en segundo plano.
        repo.saved.update { it + (2 to progress(2, 600)) }

        assertEquals(600, viewModel.state.value.progress?.totalPoints)
        assertEquals(1, repo.progressCalls)
    }

    @Test
    fun `cuantos resultados faltan subir`() {
        val repo = repository()
        val viewModel = viewModel(repo)
        viewModel.start()

        repo.pendingCount.value = 3

        assertEquals(3, viewModel.state.value.pendingCount)
    }

    @Test
    fun `si la ruta salio de lo guardado se muestra sin conexion`() {
        val repo = repository(levels = LevelsResult.Loaded(listOf(level), fromCache = true), progress = null)
        val viewModel = viewModel(repo)

        viewModel.start()

        assertTrue(viewModel.state.value.offline)
    }

    @Test
    fun `si el progreso no llega por red se muestra sin conexion, y al volver deja de estarlo`() {
        val repo = repository(progress = null)
        val viewModel = viewModel(repo)
        viewModel.start()
        assertTrue(viewModel.state.value.offline)

        repo.progress = progress(2, 300)
        viewModel.refreshProgress()

        assertFalse(viewModel.state.value.offline)
        assertEquals(300, viewModel.state.value.progress?.totalPoints)
    }

    @Test
    fun `se descargan una vez las misiones publicadas del nivel del estudiante`() {
        val repo = repository()
        val viewModel = viewModel(repo)

        viewModel.start()
        viewModel.refreshProgress()

        assertEquals(listOf(listOf(10, 11)), missions.prefetched)
    }

    @Test
    fun `sin red no se intenta descargar misiones`() {
        val repo = repository(levels = LevelsResult.Loaded(listOf(level), fromCache = true))
        val viewModel = viewModel(repo)

        viewModel.start()

        assertTrue(missions.prefetched.isEmpty())
    }

    @Test
    fun `un intento rechazado se avisa hasta que se cierra el aviso`() {
        val viewModel = viewModel(repository())
        viewModel.start()

        notices.value = listOf(RejectedNotice("a", "Primero completa la misión anterior"))
        assertEquals("Un resultado no se pudo guardar: Primero completa la misión anterior", viewModel.state.value.notice)

        viewModel.dismissNotice()
        assertNull(viewModel.state.value.notice)
    }

    @Test
    fun `varios rechazos se avisan juntos`() {
        val viewModel = viewModel(repository())
        viewModel.start()

        notices.value = listOf(RejectedNotice("a", "Motivo uno"), RejectedNotice("b", "Motivo dos"))

        assertEquals("2 resultados no se pudieron guardar: Motivo uno", viewModel.state.value.notice)
    }

    @Test
    fun `refrescar pide el progreso de nuevo sin volver a pedir la cuenta`() {
        val repo = repository()
        val viewModel = viewModel(repo)
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
        val viewModel = viewModel(repo)
        viewModel.start()

        repo.progress = null
        viewModel.refreshProgress()

        assertEquals(250, viewModel.state.value.progress?.totalPoints)
    }

    @Test
    fun `si la cuenta no carga al inicio, refrescar la vuelve a pedir`() {
        val repo = repository(user = null)
        val viewModel = viewModel(repo)
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
        val viewModel = viewModel(repo)
        viewModel.start()

        viewModel.clear()

        assertEquals(StudentHomeState(), viewModel.state.value)

        repo.user = user(7)
        repo.progress = progress(7, 10)
        viewModel.start()

        assertEquals(7, viewModel.state.value.user?.id)
        assertEquals(10, viewModel.state.value.progress?.totalPoints)
        assertEquals(2, repo.levelCalls)

        // Lo de la cuenta anterior ya no llega a la nueva.
        repo.saved.update { it + (2 to progress(2, 999)) }
        assertEquals(10, viewModel.state.value.progress?.totalPoints)
    }

    @Test
    fun `una respuesta que llega despues de cerrar sesion no deja datos de la cuenta anterior`() {
        val repo = repository()
        val gate = CompletableDeferred<Unit>()
        repo.progressGate = gate
        val viewModel = viewModel(repo)
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
        val viewModel = viewModel(repo)

        viewModel.start()
        viewModel.refreshProgress()
        assertEquals(450, viewModel.state.value.progress?.totalPoints)

        slowOld.complete(Unit) // la respuesta vieja llega después

        assertEquals(450, viewModel.state.value.progress?.totalPoints)
    }

    @Test
    fun `si no se puede saber quien es queda marcado y no se elige nivel`() {
        val viewModel = viewModel(repository(user = null))

        viewModel.start()

        val state = viewModel.state.value
        assertTrue(state.accountFailed)
        assertNull(state.user)
        assertNull(state.progress)
    }

    @Test
    fun `reintentar despues de que fallo la cuenta la vuelve a pedir`() {
        val repo = repository(user = null)
        val viewModel = viewModel(repo)
        viewModel.start()

        repo.user = user(2)
        viewModel.retry()

        val state = viewModel.state.value
        assertFalse(state.accountFailed)
        assertEquals(2, state.user?.id)
        assertEquals(250, state.progress?.totalPoints)
    }

    @Test
    fun `reintentar despues de que fallo la ruta la vuelve a pedir`() {
        val repo = repository(levels = LevelsResult.Failed("sin red"))
        val viewModel = viewModel(repo)
        viewModel.start()

        repo.levels = LevelsResult.Loaded(listOf(level))
        viewModel.retry()

        assertNull(viewModel.state.value.errorMessage)
        assertEquals(listOf(level), viewModel.state.value.levels)
        assertEquals(2, repo.levelCalls)
    }
}
