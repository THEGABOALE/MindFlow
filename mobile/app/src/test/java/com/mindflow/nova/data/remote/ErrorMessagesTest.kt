package com.mindflow.nova.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMessagesTest {

    @Test
    fun `sin internet dice que revise la red y no muestra el error tecnico`() {
        val message = connectionErrorMessage(UnknownHostException("Unable to resolve host \"mindflow-production.up.railway.app\""))

        assertEquals("No hay conexión a internet. Revisa tu red e inténtalo de nuevo.", message)
        assertFalse(message.contains("Unable"))
        assertEquals(message, connectionErrorMessage(ConnectException("failed to connect")))
    }

    @Test
    fun `si el servidor tarda lo dice distinto`() {
        assertEquals(
            "NOVA está tardando en responder. Inténtalo de nuevo en un momento.",
            connectionErrorMessage(SocketTimeoutException("timeout"))
        )
    }

    @Test
    fun `cualquier otro fallo de red usa un mensaje general`() {
        assertEquals(
            "No se pudo conectar con NOVA. Revisa tu red e inténtalo de nuevo.",
            connectionErrorMessage(IOException("unexpected end of stream"))
        )
    }

    @Test
    fun `un error del servidor no muestra el codigo ni el detalle`() {
        val raw = "{\"message\":\"Error al iniciar el intento\",\"status\":\"ERROR\",\"errorId\":\"a1b2c3d4\"}"

        assertEquals(SERVER_TROUBLE_MESSAGE, httpErrorMessage(500, raw, "No se pudo empezar la misión"))
        assertEquals(SERVER_TROUBLE_MESSAGE, httpErrorMessage(503, null, "No se pudo empezar la misión"))
    }

    @Test
    fun `un rechazo usa el mensaje del backend`() {
        val raw = "{\"message\":\"Primero completa la misión anterior\",\"status\":\"ERROR\"}"

        assertEquals("Primero completa la misión anterior", httpErrorMessage(403, raw, "No se pudo empezar la misión"))
    }

    @Test
    fun `un rechazo sin mensaje legible usa el de respaldo`() {
        assertEquals("No se pudo cargar la misión", httpErrorMessage(404, null, "No se pudo cargar la misión"))
        assertEquals("No se pudo cargar la misión", httpErrorMessage(404, "<html>404</html>", "No se pudo cargar la misión"))
        assertEquals("No se pudo cargar la misión", httpErrorMessage(404, "{\"status\":\"ERROR\"}", "No se pudo cargar la misión"))
    }
}
