package com.mindflow.nova.data.student

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.connectionErrorMessage
import com.mindflow.nova.data.remote.httpErrorMessage

/** Resultado de pedir la ruta de aprendizaje: o llegan los niveles, o el motivo por el que no. */
sealed class LevelsResult {
    data class Loaded(val levels: List<LevelResponse>) : LevelsResult()
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
}

class RemoteStudentRepository(
    private val api: NovaApiService = RetrofitClient.api
) : StudentRepository {

    override suspend fun loadLevels(): LevelsResult =
        try {
            val response = api.getLevels()

            if (response.isSuccessful) {
                LevelsResult.Loaded(response.body().orEmpty())
            } else {
                LevelsResult.Failed(
                    httpErrorMessage(
                        response.code(),
                        response.errorBody()?.string(),
                        "No se pudo cargar tu ruta de aprendizaje"
                    )
                )
            }
        } catch (e: Exception) {
            LevelsResult.Failed(connectionErrorMessage(e))
        }

    override suspend fun loadCurrentUser(): SessionUser? =
        try {
            val response = api.getMe()
            if (response.isSuccessful) response.body()?.user else null
        } catch (e: Exception) {
            null
        }

    override suspend fun loadProgress(userId: Int): StudentProgress? =
        try {
            val response = api.getStudentProgress(userId)
            if (response.isSuccessful) response.body()?.student else null
        } catch (e: Exception) {
            null
        }
}
