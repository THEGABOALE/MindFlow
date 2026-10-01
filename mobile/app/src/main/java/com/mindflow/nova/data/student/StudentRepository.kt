package com.mindflow.nova.data.student

import com.mindflow.nova.AppServices
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.local.localOrNull
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.localDay
import com.mindflow.nova.data.offline.mergePending
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.connectionErrorMessage
import com.mindflow.nova.data.remote.currentTzOffsetMinutes
import com.mindflow.nova.data.remote.httpErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Resultado de pedir la ruta de aprendizaje: o llegan los niveles, o el motivo por el que no. */
sealed class LevelsResult {
    /** [fromCache]: no hubo respuesta del servidor y son los últimos guardados en el teléfono. */
    data class Loaded(val levels: List<LevelResponse>, val fromCache: Boolean = false) : LevelsResult()
    data class Failed(val message: String) : LevelsResult()
}

/**
 * Datos del estudiante que muestran Inicio, Lecciones, Progreso y Perfil: su
 * cuenta, su ruta de aprendizaje y su avance. Las pantallas no llaman a la API;
 * se lo piden al ViewModel, y el ViewModel a este repositorio.
 */
interface StudentRepository {
    suspend fun loadLevels(): LevelsResult

    /** La cuenta con su sala, o null si no se pudo obtener. */
    suspend fun loadCurrentUser(): SessionUser?

    /** Semillas, misiones completadas, racha y avance por nivel, o null si no se pudo obtener. */
    suspend fun loadProgress(userId: Int): StudentProgress?

    /** El último progreso del servidor más lo jugado sin subir; cambia cuando cambia cualquiera de los dos. */
    fun observeProgress(userId: Int): Flow<StudentProgress?>

    /** Cuántos resultados de esta persona faltan subir. */
    fun observePendingCount(userId: Int): Flow<Int>
}

/**
 * Primero pregunta al servidor y guarda lo que llega; si no hay respuesta
 * (sin red, servidor caído) usa lo último guardado de la cuenta de la sesión.
 */
class RemoteStudentRepository(
    private val api: () -> NovaApiService = { RetrofitClient.api },
    private val local: LocalStore = AppServices.localStore,
    private val currentUserId: () -> Int? = { AppServices.session.currentUserId() },
    private val today: () -> String = { localDay(System.currentTimeMillis(), currentTzOffsetMinutes()) }
) : StudentRepository {

    override suspend fun loadLevels(): LevelsResult {
        val userId = currentUserId()

        val failure = try {
            val response = api().getLevels()

            if (response.isSuccessful) {
                val levels = response.body().orEmpty()
                userId?.let { id -> localOrNull { local.saveLevels(id, levels) } }
                return LevelsResult.Loaded(levels)
            }

            httpErrorMessage(response.code(), response.errorBody()?.string(), "No se pudo cargar tu ruta de aprendizaje")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            connectionErrorMessage(e)
        }

        val cached = userId?.let { id -> localOrNull { local.levels(id) } }

        return if (cached != null) LevelsResult.Loaded(cached, fromCache = true) else LevelsResult.Failed(failure)
    }

    override suspend fun loadCurrentUser(): SessionUser? {
        val fresh = fetchOrNull { api().getMe().let { if (it.isSuccessful) it.body()?.user else null } }

        if (fresh != null) {
            localOrNull { local.saveUser(fresh) }
            return fresh
        }

        return currentUserId()?.let { id -> localOrNull { local.user(id) } }
    }

    override suspend fun loadProgress(userId: Int): StudentProgress? {
        val fresh = fetchOrNull {
            api().getStudentProgress(userId).let { if (it.isSuccessful) it.body()?.student else null }
        }

        if (fresh != null) {
            localOrNull { local.saveProgress(userId, fresh) }
            return fresh
        }

        return localOrNull { local.progress(userId) }
    }

    override fun observeProgress(userId: Int): Flow<StudentProgress?> =
        combine(local.observeProgress(userId), local.observePending(userId)) { official, pending ->
            mergePending(official, pending, today())
        }

    override fun observePendingCount(userId: Int): Flow<Int> = local.observePending(userId).map { it.size }

    private suspend fun <T> fetchOrNull(call: suspend () -> T?): T? =
        try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
}
