package com.mindflow.nova.data.offline

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncWorkerTest {

    @Test
    fun `sin red o con el servidor fallando se reintenta`() {
        assertTrue(shouldRetry(SyncStop.NETWORK))
        assertTrue(shouldRetry(SyncStop.SERVER))
    }

    @Test
    fun `si subio todo o la sesion vencio no se reintenta`() {
        assertFalse(shouldRetry(null))
        assertFalse(shouldRetry(SyncStop.NEEDS_LOGIN))
    }
}
