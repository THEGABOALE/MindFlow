package com.mindflow.nova.data.group

import org.junit.Assert.assertEquals
import org.junit.Test

class JoinErrorMessageTest {

    private val generic = "No se pudo validar el código"

    @Test
    fun `usa el mensaje que manda el backend en el cuerpo de error`() {
        val raw = "{\"message\":\"El código no es válido, expiró o alcanzó su límite de usos\",\"status\":\"ERROR\"}"

        assertEquals("El código no es válido, expiró o alcanzó su límite de usos", joinErrorMessage(raw, null))
    }

    @Test
    fun `si el cuerpo de error no trae mensaje usa el generico`() {
        assertEquals(generic, joinErrorMessage("{\"status\":\"ERROR\"}", "otro"))
    }

    @Test
    fun `si el cuerpo de error no es JSON usa el generico`() {
        assertEquals(generic, joinErrorMessage("<html>502 Bad Gateway</html>", null))
    }

    @Test
    fun `sin cuerpo de error usa el mensaje de la respuesta`() {
        assertEquals("Respuesta rara", joinErrorMessage(null, "Respuesta rara"))
        assertEquals("Respuesta rara", joinErrorMessage("", "Respuesta rara"))
    }

    @Test
    fun `sin nada usa el generico`() {
        assertEquals(generic, joinErrorMessage(null, null))
    }
}
