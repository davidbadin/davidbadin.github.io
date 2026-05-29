package sk.punkacidetom.pd2026.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class FestivalInfoTest {

    private val start = LocalDateTime.of(2026, 5, 29, 18, 0)
    private val end   = LocalDateTime.of(2026, 5, 31, 6, 0)
    private val info  = FestivalInfo(start = start, end = end, days = emptyList())

    @Test fun phase_before() {
        assertEquals(FestivalInfo.Phase.BEFORE, info.phase(start.minusHours(1)))
    }

    @Test fun phase_during_at_start() {
        assertEquals(FestivalInfo.Phase.DURING, info.phase(start))
    }

    @Test fun phase_during_mid() {
        assertEquals(FestivalInfo.Phase.DURING, info.phase(start.plusHours(12)))
    }

    @Test fun phase_after() {
        assertEquals(FestivalInfo.Phase.AFTER, info.phase(end.plusMinutes(1)))
    }
}
