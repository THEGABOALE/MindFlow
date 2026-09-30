package com.mindflow.nova.ui.screens.auth

import com.mindflow.nova.data.group.GroupRepository
import com.mindflow.nova.data.group.JoinGroupResult
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
class JoinGroupViewModelTest {

    private class FakeRepository(var result: JoinGroupResult) : GroupRepository {
        val codes = mutableListOf<String>()
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun joinByCode(code: String): JoinGroupResult {
            codes += code
            gate?.await()
            return result
        }
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `un codigo valido deja al estudiante dentro de la sala`() {
        val repo = FakeRepository(JoinGroupResult.Joined)
        val viewModel = JoinGroupViewModel(repo)

        viewModel.join("NOVA123")

        assertEquals(JoinGroupState(joined = true), viewModel.state.value)
        assertEquals(listOf("NOVA123"), repo.codes)
    }

    @Test
    fun `un codigo rechazado deja el mensaje del backend y no entra`() {
        val viewModel = JoinGroupViewModel(FakeRepository(JoinGroupResult.Failed("El código no es válido")))

        viewModel.join("MALO")

        assertEquals("El código no es válido", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.joined)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `un codigo vacio no llama al backend`() {
        val repo = FakeRepository(JoinGroupResult.Joined)
        val viewModel = JoinGroupViewModel(repo)

        viewModel.join("   ")

        assertTrue(repo.codes.isEmpty())
        assertEquals(JoinGroupState(), viewModel.state.value)
    }

    @Test
    fun `mientras valida esta cargando y un segundo toque no manda otra peticion`() {
        val repo = FakeRepository(JoinGroupResult.Joined)
        repo.gate = CompletableDeferred()
        val viewModel = JoinGroupViewModel(repo)

        viewModel.join("NOVA123")
        assertTrue(viewModel.state.value.isLoading)

        viewModel.join("NOVA123")
        assertEquals(1, repo.codes.size)

        repo.gate?.complete(Unit)
        assertTrue(viewModel.state.value.joined)
    }

    @Test
    fun `volver a escribir quita el error del intento anterior`() {
        val viewModel = JoinGroupViewModel(FakeRepository(JoinGroupResult.Failed("El código expiró")))
        viewModel.join("VIEJO")

        viewModel.clearError()

        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `un nuevo intento limpia el error anterior mientras valida`() {
        val repo = FakeRepository(JoinGroupResult.Failed("El código expiró"))
        val viewModel = JoinGroupViewModel(repo)
        viewModel.join("VIEJO")

        repo.gate = CompletableDeferred()
        viewModel.join("NUEVO")

        assertNull(viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.isLoading)
    }
}
