package com.mindflow.nova.data.remote

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Mensajes de error que ve el estudiante. Antes se mostraba el texto técnico
// tal cual ("Error de conexión: Unable to resolve host…", "Error HTTP: 401"),
// que a un niño no le dice qué hacer.

internal const val SERVER_TROUBLE_MESSAGE = "NOVA tuvo un problema. Inténtalo de nuevo en un momento."

/** Cuando la petición ni siquiera llegó a una respuesta del servidor. */
internal fun connectionErrorMessage(error: Throwable): String = when (error) {
    is SocketTimeoutException -> "NOVA está tardando en responder. Inténtalo de nuevo en un momento."
    is UnknownHostException, is ConnectException -> "No hay conexión a internet. Revisa tu red e inténtalo de nuevo."
    else -> "No se pudo conectar con NOVA. Revisa tu red e inténtalo de nuevo."
}

private data class ApiError(val message: String?)

/**
 * Cuando el servidor respondió con un error. Si es un fallo del servidor (5xx)
 * se dice eso, sin detalles. Si es un rechazo (4xx) se usa el mensaje del
 * backend, que ya está escrito para mostrarse ("Primero completa la misión
 * anterior"); si no trae uno, [fallback].
 */
internal fun httpErrorMessage(httpCode: Int, rawError: String?, fallback: String, gson: Gson = Gson()): String {
    if (httpCode >= 500) {
        return SERVER_TROUBLE_MESSAGE
    }

    if (rawError.isNullOrBlank()) {
        return fallback
    }

    return try {
        gson.fromJson(rawError, ApiError::class.java)?.message ?: fallback
    } catch (e: JsonSyntaxException) {
        fallback
    }
}
