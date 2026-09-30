package com.mindflow.nova.data.group

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mindflow.nova.data.model.JoinGroupRequest
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient

/** Resultado de intentar entrar a una sala con su código. */
sealed class JoinGroupResult {
    object Joined : JoinGroupResult()
    data class Failed(val message: String) : JoinGroupResult()
}

/** Salas del estudiante. La pantalla del código no llama a la API; pasa por el ViewModel y este repositorio. */
interface GroupRepository {
    suspend fun joinByCode(code: String): JoinGroupResult
}

class RemoteGroupRepository(
    private val api: NovaApiService = RetrofitClient.api
) : GroupRepository {

    private val gson = Gson()

    override suspend fun joinByCode(code: String): JoinGroupResult =
        try {
            val response = api.joinGroupByCode(JoinGroupRequest(code.trim()))
            val body = response.body()

            if (response.isSuccessful && body?.status == "OK") {
                JoinGroupResult.Joined
            } else {
                JoinGroupResult.Failed(joinErrorMessage(response.errorBody()?.string(), body?.message, gson))
            }
        } catch (e: Exception) {
            JoinGroupResult.Failed("Error de conexión: ${e.message}")
        }
}

private const val DEFAULT_JOIN_ERROR = "No se pudo validar el código"

private data class ApiError(val message: String?)

/**
 * El motivo por el que el backend rechazó el código. En una respuesta de error
 * el JSON llega en el cuerpo de error, no en el normal, así que se lee de ahí
 * primero para no perder el mensaje real ("El código no es válido, expiró...").
 */
internal fun joinErrorMessage(rawError: String?, bodyMessage: String?, gson: Gson = Gson()): String {
    if (!rawError.isNullOrEmpty()) {
        return try {
            gson.fromJson(rawError, ApiError::class.java)?.message ?: DEFAULT_JOIN_ERROR
        } catch (e: JsonSyntaxException) {
            DEFAULT_JOIN_ERROR
        }
    }

    return bodyMessage ?: DEFAULT_JOIN_ERROR
}
