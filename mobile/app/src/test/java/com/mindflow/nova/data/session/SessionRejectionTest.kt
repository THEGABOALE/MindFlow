package com.mindflow.nova.data.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRejectionTest {

    @Test
    fun `401 y 403 significan que el token ya no sirve`() {
        assertTrue(isSessionRejected(401))
        assertTrue(isSessionRejected(403))
    }

    @Test
    fun `los errores del servidor no cierran la sesion`() {
        listOf(500, 502, 503, 504, 404, 429).forEach { code ->
            assertFalse("HTTP $code no debería cerrar la sesión", isSessionRejected(code))
        }
    }
}
