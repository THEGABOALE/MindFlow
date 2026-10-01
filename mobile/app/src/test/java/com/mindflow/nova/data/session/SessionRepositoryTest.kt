package com.mindflow.nova.data.session

import com.mindflow.nova.data.local.FakeLocalStore
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.LoginResponse
import com.mindflow.nova.data.model.MeResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.PendingAttempt
import com.mindflow.nova.data.remote.FakeNovaApi
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.httpError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class SessionRepositoryTest {

    private class FakeTokenStore : TokenStore {
        var savedToken: String? = null
        var savedUserId: Int? = null

        override fun getToken(): String? = savedToken
        override fun saveToken(token: String) { savedToken = token }
        override fun getUserId(): Int? = savedUserId
        override fun saveUserId(userId: Int) { savedUserId = userId }
        override fun clear() { savedToken = null; savedUserId = null }
    }

    private val user = SessionUser(
        id = 7, fullName = "Ana", email = null, loginId = "ana7", role = "student", centerId = 1, group = null
    )

    private val storage = FakeTokenStore()
    private val local = FakeLocalStore()
    private val api = FakeNovaApi()
    // El borrado de la caché al cerrar sesión corre en el hilo del test.
    private val session = SessionRepository(storage, local, api = { api }, ioDispatcher = Dispatchers.Unconfined)

    private fun loggedIn() {
        storage.savedToken = "tok"
        storage.savedUserId = user.id
    }

    @Test
    fun `iniciar sesion guarda el token, quien es y su cuenta`() = runTest {
        api.onLoginId = { Response.success(LoginResponse("ok", "OK", "tok", user)) }

        val result = session.loginWithId("ana7", "clave")

        assertEquals(SessionResult.Success(user), result)
        assertEquals("tok", storage.savedToken)
        assertEquals(7, session.currentUserId())
        assertEquals(user, local.user(7))
    }

    @Test
    fun `con red la sesion se valida y la cuenta guardada se actualiza`() = runTest {
        loggedIn()
        val renamed = user.copy(fullName = "Ana María")
        api.onMe = { Response.success(MeResponse("ok", "OK", renamed)) }

        assertEquals(SessionResult.Success(renamed, offline = false), session.restoreSession())
        assertEquals(renamed, local.user(7))
    }

    @Test
    fun `sin red entra con la cuenta guardada`() = runTest {
        loggedIn()
        local.saveUser(user)
        api.onMe = { throw IOException("sin red") }

        assertEquals(SessionResult.Success(user, offline = true), session.restoreSession())
        assertEquals("tok", storage.savedToken)
    }

    @Test
    fun `si el servidor falla tambien entra con la cuenta guardada`() = runTest {
        loggedIn()
        local.saveUser(user)
        api.onMe = { httpError(503) }

        assertEquals(SessionResult.Success(user, offline = true), session.restoreSession())
    }

    @Test
    fun `sin red y sin cuenta guardada no puede entrar`() = runTest {
        loggedIn()
        api.onMe = { throw IOException("sin red") }

        assertTrue(session.restoreSession() is SessionResult.Failure)
        assertEquals("tok", storage.savedToken)
    }

    @Test
    fun `sin sesion guardada va al login`() = runTest {
        assertTrue(session.restoreSession() is SessionResult.Rejected)
    }

    @Test
    fun `con el token vencido se cierra la sesion pero los pendientes quedan`() = runTest {
        loggedIn()
        local.saveUser(user)
        local.addPending(pendingOf(user.id))
        api.onMe = { httpError(401) }

        assertTrue(session.restoreSession() is SessionResult.Rejected)
        assertNull(storage.savedToken)
        assertNull(session.currentUserId())
        assertEquals(1, local.pendingNow(user.id).size)
    }

    @Test
    fun `cerrar sesion borra la cuenta del telefono pero no los pendientes`() = runTest {
        loggedIn()
        local.saveUser(user)
        local.saveProgress(user.id, StudentProgress(7, "Ana", 10, 1, listOf(1), null, emptyList()))
        local.addPending(pendingOf(user.id))

        session.logout()

        assertNull(storage.savedToken)
        assertEquals(listOf(7), local.clearedAccounts)
        assertNull(local.user(7))
        assertNull(local.progress(7))
        assertEquals(1, local.pendingNow(user.id).size)
    }

    private fun pendingOf(userId: Int) = PendingAttempt(
        clientAttemptId = "a", userId = userId, missionId = 2,
        startedAt = "2026-10-01T10:00:00.000-06:00", finishedAt = "2026-10-01T10:05:00.000-06:00",
        tzOffsetMinutes = -360, timedOut = false, answers = emptyList(),
        provisional = AttemptResult(0, 2, 100, 3, 0, 3, 100, false, "completed")
    )
}
