package com.mindflow.nova.data.student

import com.mindflow.nova.data.local.FakeLocalStore
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MeResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.model.StudentProgressResponse
import com.mindflow.nova.data.offline.PendingAttempt
import com.mindflow.nova.data.remote.FakeNovaApi
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.httpError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class RemoteStudentRepositoryTest {

    private val userId = 7
    private val api = FakeNovaApi()
    private val local = FakeLocalStore()
    private var sessionUserId: Int? = userId
    private val repository = RemoteStudentRepository(
        api = { api }, local = local, currentUserId = { sessionUserId }, today = { "2026-10-01" }
    )

    private val levels = listOf(LevelResponse(1, "Primaria alta", "primaria_alta", null, 1, emptyList()))
    private val user = SessionUser(userId, "Ana", null, "ana7", "student", 1, null)
    private val progress = StudentProgress(userId, "Ana", 100, 1, listOf(1), null, emptyList())

    @Test
    fun `los niveles que llegan por red se guardan`() = runTest {
        api.onLevels = { Response.success(levels) }

        assertEquals(LevelsResult.Loaded(levels, fromCache = false), repository.loadLevels())
        assertEquals(levels, local.levels(userId))
    }

    @Test
    fun `sin red los niveles salen de lo guardado`() = runTest {
        local.saveLevels(userId, levels)
        api.onLevels = { throw IOException("sin red") }

        assertEquals(LevelsResult.Loaded(levels, fromCache = true), repository.loadLevels())
    }

    @Test
    fun `los niveles guardados de otra cuenta no se usan`() = runTest {
        local.saveLevels(99, levels)
        api.onLevels = { throw IOException("sin red") }

        val result = repository.loadLevels()

        assertEquals(
            LevelsResult.Failed("No se pudo conectar con NOVA. Revisa tu red e inténtalo de nuevo."),
            result
        )
    }

    @Test
    fun `si el servidor falla y no hay nada guardado se muestra el error de siempre`() = runTest {
        api.onLevels = { httpError(503) }

        assertEquals(LevelsResult.Failed("NOVA tuvo un problema. Inténtalo de nuevo en un momento."), repository.loadLevels())
    }

    @Test
    fun `sin sesion no se guarda nada`() = runTest {
        sessionUserId = null
        api.onLevels = { Response.success(levels) }

        repository.loadLevels()

        assertNull(local.levels(userId))
    }

    @Test
    fun `la cuenta por red se guarda y sin red sale de lo guardado`() = runTest {
        api.onMe = { Response.success(MeResponse("ok", "OK", user)) }
        assertEquals(user, repository.loadCurrentUser())
        assertEquals(user, local.user(userId))

        api.onMe = { throw IOException("sin red") }
        assertEquals(user, repository.loadCurrentUser())
    }

    @Test
    fun `sin red ni cuenta guardada no hay cuenta`() = runTest {
        api.onMe = { throw IOException("sin red") }

        assertNull(repository.loadCurrentUser())
    }

    @Test
    fun `el progreso por red se guarda y sin red sale de lo guardado`() = runTest {
        api.onProgress = { Response.success(StudentProgressResponse("ok", "OK", progress)) }
        assertEquals(progress, repository.loadProgress(userId))
        assertEquals(progress, local.progress(userId))

        api.onProgress = { httpError(500) }
        assertEquals(progress, repository.loadProgress(userId))
    }

    @Test
    fun `el progreso observado suma lo que falta subir`() = runTest {
        local.saveProgress(userId, progress)
        local.addPending(
            PendingAttempt(
                clientAttemptId = "a", userId = userId, missionId = 2,
                startedAt = "2026-10-01T10:00:00.000-06:00", finishedAt = "2026-10-01T10:05:00.000-06:00",
                tzOffsetMinutes = -360, timedOut = false, answers = emptyList(),
                provisional = AttemptResult(0, 2, 100, 3, 0, 3, 75, false, "completed")
            )
        )

        val observed = repository.observeProgress(userId).first()!!

        assertEquals(175, observed.totalPoints)
        assertEquals(listOf(1, 2), observed.completedMissionIds)
        assertEquals(1, repository.observePendingCount(userId).first())
    }
}
