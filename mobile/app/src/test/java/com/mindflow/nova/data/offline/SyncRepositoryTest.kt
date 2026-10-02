package com.mindflow.nova.data.offline

import com.mindflow.nova.data.local.FakeLocalStore
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.model.StudentStreak
import com.mindflow.nova.data.model.SyncProgress
import com.mindflow.nova.data.remote.FakeNovaApi
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.acceptedResult
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.httpError
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.rejectedResult
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.syncOk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SyncRepositoryTest {

    private val userId = 7
    private val store = FakeLocalStore()
    private val api = FakeNovaApi()
    /** Qué cuenta tiene la sesión abierta y con qué token. */
    private var session: Pair<Int, String>? = userId to "token-ana"
    private val sync = SyncRepository(
        api = { api }, store = store, tokenFor = { id -> session?.takeIf { it.first == id }?.second }, tzOffset = { -360 }
    )

    private fun pending(id: String, missionId: Int = 2, minute: Int = 0, user: Int = userId) = PendingAttempt(
        clientAttemptId = id,
        userId = user,
        missionId = missionId,
        startedAt = "2026-10-01T10:00:00.000-06:00",
        finishedAt = "2026-10-01T10:%02d:00.000-06:00".format(minute),
        tzOffsetMinutes = -360,
        timedOut = false,
        answers = emptyList(),
        provisional = AttemptResult(
            id = 0, missionId = missionId, score = 100, correctAnswers = 3, wrongAnswers = 0,
            plumasLeft = 3, pointsEarned = 100, isReview = false, status = "completed"
        )
    )

    @Test
    fun `lo aceptado sale de la cola`() = runTest {
        store.addPending(pending("a", missionId = 2))

        val report = sync.syncPending(userId)

        assertEquals(setOf("a"), report.accepted.keys)
        assertEquals(2, report.accepted.getValue("a").missionId)
        assertNull(report.stoppedBy)
        assertTrue(store.pendingNow(userId).isEmpty())
        assertEquals(-360, api.syncRequests.single().tzOffsetMinutes)
        assertEquals("2026-10-01T10:00:00.000-06:00", api.syncRequests.single().attempts.single().finishedAt)
    }

    @Test
    fun `lo rechazado sale de la cola y deja un aviso`() = runTest {
        store.addPending(pending("a", minute = 1))
        store.addPending(pending("b", minute = 2))
        api.onSync = { syncOk(listOf(acceptedResult("a", 2), rejectedResult("b", "Primero completa la misión anterior"))) }

        val report = sync.syncPending(userId)

        assertEquals(mapOf("b" to "Primero completa la misión anterior"), report.rejected)
        assertTrue(store.pendingNow(userId).isEmpty())
        assertEquals(listOf(RejectedNotice("b", "Primero completa la misión anterior")), sync.rejectedNotices(userId).first())

        sync.consumeNotices(userId)
        assertTrue(sync.rejectedNotices(userId).first().isEmpty())
    }

    @Test
    fun `el aviso queda guardado aunque se cierre la app antes de verlo`() = runTest {
        store.addPending(pending("b"))
        api.onSync = { syncOk(listOf(rejectedResult("b", "Primero completa la misión anterior"))) }
        sync.syncPending(userId)

        // Otra apertura de la app: una SyncRepository nueva sobre el mismo almacenamiento.
        val afterRestart = SyncRepository(api = { api }, store = store, tokenFor = { "token-ana" })

        assertEquals(listOf(RejectedNotice("b", "Primero completa la misión anterior")), afterRestart.rejectedNotices(userId).first())
        // Y es solo de su cuenta.
        assertTrue(afterRestart.rejectedNotices(99).first().isEmpty())
    }

    @Test
    fun `el aviso de un rechazo ya mostrado se puede quitar`() = runTest {
        store.addPending(pending("a", minute = 1))
        store.addPending(pending("b", minute = 2))
        api.onSync = { syncOk(listOf(rejectedResult("a", "uno"), rejectedResult("b", "dos"))) }
        sync.syncPending(userId)

        sync.dismissNotice("a")

        assertEquals(listOf(RejectedNotice("b", "dos")), sync.rejectedNotices(userId).first())
    }

    @Test
    fun `sin red no se borra nada`() = runTest {
        store.addPending(pending("a"))
        api.onSync = { throw IOException("sin red") }

        val report = sync.syncPending(userId)

        assertEquals(SyncStop.NETWORK, report.stoppedBy)
        assertEquals(listOf("a"), store.pendingNow(userId).map { it.clientAttemptId })
    }

    @Test
    fun `un error del servidor detiene la subida`() = runTest {
        store.addPending(pending("a"))
        api.onSync = { httpError(500) }

        val report = sync.syncPending(userId)

        assertEquals(SyncStop.SERVER, report.stoppedBy)
        assertEquals(1, store.pendingNow(userId).size)
    }

    @Test
    fun `con la sesion vencida se pide entrar de nuevo y la cola queda`() = runTest {
        store.addPending(pending("a"))
        api.onSync = { httpError(401) }

        val report = sync.syncPending(userId)

        assertEquals(SyncStop.NEEDS_LOGIN, report.stoppedBy)
        assertEquals(1, store.pendingNow(userId).size)
    }

    @Test
    fun `120 pendientes van en lotes de 50, 50 y 20 en orden`() = runTest {
        // Se agregan al revés para comprobar que se mandan por finishedAt.
        (119 downTo 0).forEach { i -> store.addPending(pending("id-%03d".format(i), minute = 0).copy(
            finishedAt = "2026-10-01T%02d:%02d:00.000-06:00".format(i / 60, i % 60)
        )) }

        val report = sync.syncPending(userId)

        assertEquals(listOf(50, 50, 20), api.syncRequests.map { it.attempts.size })
        val sent = api.syncRequests.flatMap { batch -> batch.attempts.map { it.clientAttemptId } }
        assertEquals((0 until 120).map { "id-%03d".format(it) }, sent)
        assertEquals(120, report.accepted.size)
        assertTrue(store.pendingNow(userId).isEmpty())
    }

    @Test
    fun `si falla un lote los siguientes no se mandan`() = runTest {
        (0 until 60).forEach { store.addPending(pending("id-%03d".format(it)).copy(
            finishedAt = "2026-10-01T10:%02d:00.000-06:00".format(it)
        )) }
        var calls = 0
        val defaultSync = api.onSync
        api.onSync = { request -> if (calls++ == 0) defaultSync(request) else throw IOException() }

        val report = sync.syncPending(userId)

        assertEquals(50, report.accepted.size)
        assertEquals(SyncStop.NETWORK, report.stoppedBy)
        assertEquals(10, store.pendingNow(userId).size)
    }

    @Test
    fun `el progreso guardado toma lo que manda el servidor`() = runTest {
        store.saveProgress(userId, StudentProgress(
            id = userId, fullName = "Ana", totalPoints = 0, missionsCompleted = 0,
            completedMissionIds = emptyList(), streak = null, levels = emptyList()
        ))
        store.addPending(pending("a"))
        val streak = StudentStreak(days = 1, isActive = true, lastActivityDate = "2026-10-01")
        api.onSync = {
            syncOk(
                listOf(acceptedResult("a", 2, pointsEarned = 150)),
                SyncProgress(totalPoints = 150, missionsCompleted = 1, completedMissionIds = listOf(2), streak = streak, justActivatedStreak = true)
            )
        }

        val report = sync.syncPending(userId)
        val saved = store.progress(userId)!!

        assertEquals(150, saved.totalPoints)
        assertEquals(1, saved.missionsCompleted)
        assertEquals(listOf(2), saved.completedMissionIds)
        assertEquals(streak, saved.streak)
        assertEquals("Ana", saved.fullName)
        assertTrue(report.progress!!.justActivatedStreak)
    }

    @Test
    fun `los intentos van con el token de su cuenta`() = runTest {
        store.addPending(pending("a"))

        sync.syncPending(userId)

        assertEquals(listOf("Bearer token-ana"), api.syncAuthorizations)
    }

    @Test
    fun `si la sesion ya no es de esa cuenta no se manda nada`() = runTest {
        store.addPending(pending("a"))
        session = 99 to "token-otra"

        val report = sync.syncPending(userId)

        assertEquals(SyncStop.NEEDS_LOGIN, report.stoppedBy)
        assertTrue(api.syncRequests.isEmpty())
        assertEquals(1, store.pendingNow(userId).size)
    }

    @Test
    fun `si se cambia de cuenta a mitad de la subida lo que falta sigue a nombre de la primera`() = runTest {
        (0 until 60).forEach { store.addPending(pending("id-%03d".format(it)).copy(
            finishedAt = "2026-10-01T10:%02d:00.000-06:00".format(it)
        )) }
        val defaultSync = api.onSync
        api.onSync = { request ->
            // Mientras se sube el primer lote, alguien cierra sesión y entra otra cuenta.
            session = 99 to "token-otra"
            defaultSync(request)
        }

        sync.syncPending(userId)

        assertEquals(listOf("Bearer token-ana", "Bearer token-ana"), api.syncAuthorizations)
        assertTrue(store.pendingNow(userId).isEmpty())
    }

    @Test
    fun `sin pendientes no llama al servidor`() = runTest {
        store.addPending(pending("de-otra-cuenta", user = 99))

        val report = sync.syncPending(userId)

        assertTrue(api.syncRequests.isEmpty())
        assertTrue(report.accepted.isEmpty())
        assertNull(report.stoppedBy)
    }

    @Test
    fun `el servidor puede devolver el id en otro formato de mayusculas`() = runTest {
        store.addPending(pending("ABC-1"))
        api.onSync = { syncOk(listOf(acceptedResult("abc-1", 2))) }

        val report = sync.syncPending(userId)

        assertEquals(setOf("ABC-1"), report.accepted.keys)
        assertTrue(store.pendingNow(userId).isEmpty())
    }

    @Test
    fun `dos subidas a la vez no mandan dos veces lo mismo`() = runTest {
        store.addPending(pending("a"))
        val gate = CompletableDeferred<Unit>()
        val defaultSync = api.onSync
        api.onSync = { request -> gate.await(); defaultSync(request) }

        val first = async { sync.syncPending(userId) }
        val second = async { sync.syncPending(userId) }
        // Que las dos arranquen antes de que el servidor conteste.
        testScheduler.runCurrent()
        gate.complete(Unit)
        val reports = listOf(first, second).awaitAll()

        assertEquals(1, api.syncRequests.size)
        assertEquals(1, reports.sumOf { it.accepted.size })
    }

    @Test
    fun `syncNow devuelve null si el servidor no contesta a tiempo`() = runTest {
        store.addPending(pending("a"))
        api.onSync = { CompletableDeferred<Unit>().await(); error("no llega") }

        assertNull(sync.syncNow(userId, timeoutMs = 1_000))
        assertEquals(1, store.pendingNow(userId).size)
    }
}
