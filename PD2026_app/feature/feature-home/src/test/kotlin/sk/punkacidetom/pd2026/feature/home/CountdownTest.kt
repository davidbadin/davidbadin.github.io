package sk.punkacidetom.pd2026.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownTest {

    private fun countdownFrom(totalSeconds: Long): CountdownState {
        val s = totalSeconds
        return CountdownState(
            days    = s / 86400,
            hours   = (s % 86400) / 3600,
            minutes = (s % 3600) / 60,
        )
    }

    @Test fun exactly_one_day() {
        val c = countdownFrom(86400)
        assertEquals(1L, c.days)
        assertEquals(0L, c.hours)
        assertEquals(0L, c.minutes)
    }

    @Test fun two_days_three_hours_seven_minutes() {
        val total = 2 * 86400L + 3 * 3600L + 7 * 60L + 5L
        val c = countdownFrom(total)
        assertEquals(2L, c.days)
        assertEquals(3L, c.hours)
        assertEquals(7L, c.minutes)
    }

    @Test fun zero_seconds() {
        val c = countdownFrom(0)
        assertEquals(CountdownState(0, 0, 0), c)
    }
}
