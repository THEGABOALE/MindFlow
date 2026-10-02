package com.mindflow.nova.data.mission

import com.mindflow.nova.data.local.FakeLocalStore
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionContentResponse
import com.mindflow.nova.data.remote.FakeNovaApi
import com.mindflow.nova.data.remote.FakeNovaApi.Companion.httpError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class RemoteMissionRepositoryTest {

    private val hourMs = 60 * 60 * 1000L
    private var clock = 100 * hourMs
    private val api = FakeNovaApi()
    private val local = FakeLocalStore(now = { clock })
    private val repository = RemoteMissionRepository(api = { api }, local = local, now = { clock })

    private fun content(id: Int, title: String = "Misión $id") = MissionContent(
        id = id, levelId = 1, title = title, description = null, topic = null, orderIndex = id,
        pointsReward = 100, mechanic = "multiple_choice", timeLimitSeconds = null, maxPlumas = 3,
        questions = emptyList()
    )

    private fun ok(id: Int) = Response.success(MissionContentResponse("ok", "OK", content(id)))

    @Test
    fun `el contenido que llega por red se guarda`() = runTest {
        api.onMissionContent = { ok(it) }

        assertEquals(MissionContentResult.Loaded(content(3)), repository.loadContent(3))
        assertEquals(content(3), local.mission(3))
    }

    @Test
    fun `sin red el contenido sale de lo guardado`() = runTest {
        local.saveMission(content(3))
        api.onMissionContent = { throw IOException("sin red") }

        assertEquals(MissionContentResult.Loaded(content(3)), repository.loadContent(3))
    }

    @Test
    fun `sin red y sin descargar se explica que hay que conectarse`() = runTest {
        api.onMissionContent = { throw IOException("sin red") }

        assertEquals(
            MissionContentResult.Failed("Esta misión todavía no está descargada. Conéctate a internet para bajarla."),
            repository.loadContent(3)
        )
    }

    @Test
    fun `si el servidor dice que la mision no existe no se usa la copia guardada`() = runTest {
        local.saveMission(content(3))
        api.onMissionContent = { httpError(404) }

        assertEquals(MissionContentResult.Failed("x"), repository.loadContent(3))
    }

    @Test
    fun `la descarga previa baja solo las que faltan o tienen mas de 24 horas`() = runTest {
        api.onMissionContent = { ok(it) }
        local.saveMission(content(1, "vieja"))
        clock += 25 * hourMs
        local.saveMission(content(2, "reciente"))
        clock += hourMs

        repository.prefetch(listOf(1, 2, 3, 3))

        assertEquals(listOf(1, 3), api.missionRequests)
        assertEquals("Misión 1", local.mission(1)!!.title)
        assertEquals("reciente", local.mission(2)!!.title)
    }

    @Test
    fun `la descarga previa sigue aunque una falle`() = runTest {
        api.onMissionContent = { id -> if (id == 1) throw IOException("cortada") else ok(id) }

        repository.prefetch(listOf(1, 2))

        assertNull(local.mission(1))
        assertEquals(content(2), local.mission(2))
    }
}
