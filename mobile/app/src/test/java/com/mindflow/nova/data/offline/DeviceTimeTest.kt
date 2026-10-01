package com.mindflow.nova.data.offline

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceTimeTest {

    // 2026-10-01T18:26:04.000Z
    private val instant = 1790879164000L

    @Test
    fun `la hora va con el huso del dispositivo`() {
        assertEquals("2026-10-01T12:26:04.000-06:00", isoWithOffset(instant, -360))
        assertEquals("2026-10-01T23:56:04.000+05:30", isoWithOffset(instant, 330))
        assertEquals("2026-10-01T18:26:04.000+00:00", isoWithOffset(instant, 0))
    }

    @Test
    fun `el dia local cambia a la medianoche del dispositivo, no a la de UTC`() {
        assertEquals("2026-10-01", localDay(instant, -360))
        assertEquals("2026-10-01", localDay(instant, 330))
        // Una hora después ya es 2 de octubre en India, pero sigue siendo 1 en Costa Rica.
        assertEquals("2026-10-02", localDay(instant + 3_600_000, 330))
        assertEquals("2026-10-01", localDay(instant + 3_600_000, -360))
        // A las 11 de la noche de Costa Rica ya es el día siguiente en UTC.
        assertEquals("2026-10-01", localDay(1790917200000L, -360))
    }

    @Test
    fun `ida y vuelta entre epoch e ISO`() {
        for (offset in listOf(-360, 0, 330, 840, -720)) {
            assertEquals(instant, epochFromIso(isoWithOffset(instant, offset)))
        }
        assertEquals(instant, epochFromIso("2026-10-01T18:26:04.000Z"))
    }
}
