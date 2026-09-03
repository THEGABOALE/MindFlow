package com.mindflow.nova.data.session

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mindflow.nova.data.model.LoginGoogleRequest
import com.mindflow.nova.data.model.LoginIdRequest
import com.mindflow.nova.data.model.LoginResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.remote.RetrofitClient
import retrofit2.Response

/** Resultado de un intento de login o de restaurar la sesión guardada. */
sealed class SessionResult {
    data class Success(val user: SessionUser) : SessionResult()
    /** El backend respondió, pero rechazó: credenciales malas, cuenta inactiva, etc. */
    data class Rejected(val message: String) : SessionResult()
    data class Failure(val message: String) : SessionResult()
}

/**
 * Punto único de entrada para iniciar sesión, restaurarla al abrir la app y
 * cerrarla. La pantalla de login se conecta acá cuando exista el wireframe.
 */
class SessionRepository(private val storage: SessionStorage) {

    private val gson = Gson()

    init {
        // Desde acá el interceptor ya puede firmar cada petición.
        RetrofitClient.tokenProvider = { storage.getToken() }
    }

    fun hasStoredToken(): Boolean = !storage.getToken().isNullOrBlank()

    suspend fun loginWithId(loginId: String, password: String): SessionResult =
        runLogin {
            RetrofitClient.api.loginWithId(LoginIdRequest(loginId.trim(), password))
        }

    suspend fun loginWithGoogle(idToken: String): SessionResult =
        runLogin {
            RetrofitClient.api.loginWithGoogle(LoginGoogleRequest(idToken))
        }

    /**
     * Valida contra el backend el token que quedó guardado. Sirve para decidir
     * al abrir la app si se va directo al home o a la pantalla de login.
     */
    suspend fun restoreSession(): SessionResult {
        if (!hasStoredToken()) {
            return SessionResult.Rejected("No hay sesión guardada")
        }

        return try {
            val response = RetrofitClient.api.getMe()
            val user = response.body()?.user

            if (response.isSuccessful && user != null) {
                SessionResult.Success(user)
            } else {
                // Un 401 acá significa token vencido, o cuenta desactivada o
                // con el rol cambiado desde que se inició sesión.
                logout()
                SessionResult.Rejected(errorMessage(response, "La sesión ya no es válida"))
            }
        } catch (e: Exception) {
            SessionResult.Failure("Error de conexión: ${e.message}")
        }
    }

    fun logout() {
        storage.clear()
    }

    private suspend fun runLogin(
        call: suspend () -> Response<LoginResponse>
    ): SessionResult {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body?.token != null && body.user != null) {
                storage.saveToken(body.token)
                SessionResult.Success(body.user)
            } else {
                SessionResult.Rejected(errorMessage(response, "No se pudo iniciar sesión"))
            }
        } catch (e: Exception) {
            SessionResult.Failure("Error de conexión: ${e.message}")
        }
    }

    /**
     * En una respuesta de error Retrofit deja el JSON en errorBody(), no en
     * body(), así que sin esto se perdería el mensaje real del backend
     * (por ejemplo "ID o contraseña incorrectos").
     */
    private fun errorMessage(response: Response<*>, fallback: String): String {
        val raw = try {
            response.errorBody()?.string()
        } catch (e: Exception) {
            null
        }

        if (raw.isNullOrBlank()) {
            return fallback
        }

        return try {
            gson.fromJson(raw, ApiError::class.java)?.message ?: fallback
        } catch (e: JsonSyntaxException) {
            fallback
        }
    }

    private data class ApiError(val message: String?, val status: String?)
}
