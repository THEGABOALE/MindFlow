package com.mindflow.nova.data.session

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.model.LoginGoogleRequest
import com.mindflow.nova.data.model.LoginIdRequest
import com.mindflow.nova.data.model.LoginResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.SERVER_TROUBLE_MESSAGE
import com.mindflow.nova.data.remote.connectionErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Response

/** Resultado de un intento de login o de restaurar la sesión guardada. */
sealed class SessionResult {
    /**
     * [offline]: no se pudo validar con el servidor (sin red o caído) y se
     * entró con la cuenta guardada en el teléfono.
     */
    data class Success(val user: SessionUser, val offline: Boolean = false) : SessionResult()
    /** El backend respondió, pero rechazó: credenciales malas, cuenta inactiva, etc. */
    data class Rejected(val message: String) : SessionResult()
    data class Failure(val message: String) : SessionResult()
}

/**
 * Punto único de entrada para iniciar sesión, restaurarla al abrir la app y
 * cerrarla. Sin conexión, abre con la última cuenta que entró en el teléfono.
 */
class SessionRepository(
    private val storage: TokenStore,
    private val local: LocalStore,
    private val api: () -> NovaApiService = { RetrofitClient.api },
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val gson = Gson()

    // logout() se llama desde la UI sin corrutina; el borrado de la caché va acá.
    private val cleanupScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        // Desde acá el interceptor ya puede firmar cada petición.
        RetrofitClient.tokenProvider = { storage.getToken() }
    }

    fun hasStoredToken(): Boolean = !storage.getToken().isNullOrBlank()

    /** De quién es la sesión guardada, o null si no hay. */
    fun currentUserId(): Int? = storage.getUserId()

    suspend fun loginWithId(loginId: String, password: String): SessionResult =
        runLogin {
            api().loginWithId(LoginIdRequest(loginId.trim(), password))
        }

    suspend fun loginWithGoogle(idToken: String): SessionResult =
        runLogin {
            api().loginWithGoogle(LoginGoogleRequest(idToken))
        }

    /**
     * Valida contra el backend el token que quedó guardado. Sirve para decidir
     * al abrir la app si se va directo al home o a la pantalla de login.
     *
     * Si no hay red o el servidor falla, entra con la cuenta guardada (offline):
     * el token se conserva y, si venció, la subida de resultados lo descubre
     * cuando vuelva la conexión.
     */
    suspend fun restoreSession(): SessionResult {
        if (!hasStoredToken()) {
            return SessionResult.Rejected("No hay sesión guardada")
        }

        return try {
            val response = api().getMe()
            val user = response.body()?.user

            when {
                response.isSuccessful && user != null -> {
                    remember(user)
                    SessionResult.Success(user)
                }

                // Token vencido, o cuenta desactivada o con el rol cambiado
                // desde que se inició sesión: ahí sí hay que volver a entrar.
                isSessionRejected(response.code()) -> {
                    logout()
                    SessionResult.Rejected(errorMessage(response, "La sesión ya no es válida"))
                }

                // Cualquier otro error (502/503 mientras Railway arranca, un
                // fallo puntual de la base) no dice nada del token: se conserva
                // para reintentar en vez de sacar a la persona de su cuenta.
                else -> offlineOr(SERVER_TROUBLE_MESSAGE)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            offlineOr(connectionErrorMessage(e))
        }
    }

    /**
     * Cierra la sesión y borra del teléfono la cuenta, la ruta y el progreso
     * guardados. Los resultados que falta subir se quedan: se suben cuando esa
     * persona vuelva a entrar.
     */
    fun logout() {
        val userId = storage.getUserId()
        storage.clear()

        if (userId != null) {
            cleanupScope.launch { runCatching { local.clearAccount(userId) } }
        }
    }

    /** La cuenta guardada de la sesión, o [message] si nunca se guardó una. */
    private suspend fun offlineOr(message: String): SessionResult {
        val cached = storage.getUserId()?.let { id -> runCatching { local.user(id) }.getOrNull() }

        return if (cached != null) SessionResult.Success(cached, offline = true) else SessionResult.Failure(message)
    }

    private suspend fun remember(user: SessionUser) {
        storage.saveUserId(user.id)
        // Si no se pudo guardar, solo se pierde poder abrir sin conexión.
        runCatching { local.saveUser(user) }
    }

    private suspend fun runLogin(
        call: suspend () -> Response<LoginResponse>
    ): SessionResult {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body?.token != null && body.user != null) {
                storage.saveToken(body.token)
                remember(body.user)
                SessionResult.Success(body.user)
            } else {
                SessionResult.Rejected(
                    if (response.code() >= 500) SERVER_TROUBLE_MESSAGE
                    else errorMessage(response, "No se pudo iniciar sesión")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SessionResult.Failure(connectionErrorMessage(e))
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

/** Solo 401/403 significan que el token guardado ya no sirve; lo demás es un fallo del servidor o de la red. */
internal fun isSessionRejected(httpCode: Int): Boolean = httpCode == 401 || httpCode == 403
