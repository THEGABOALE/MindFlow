package com.mindflow.nova.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatPairTest {

    @Test
    fun `con la fuente normal o un poco grande van lado a lado`() {
        assertFalse(stackStats(1.0f))
        assertFalse(stackStats(1.3f))
    }

    @Test
    fun `con la fuente grande van una debajo de la otra`() {
        assertTrue(stackStats(1.5f))
        assertTrue(stackStats(2.0f))
    }
}
