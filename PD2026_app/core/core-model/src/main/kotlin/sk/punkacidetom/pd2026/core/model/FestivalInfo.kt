package sk.punkacidetom.pd2026.core.model

import java.time.LocalDateTime

data class FestivalInfo(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val days: List<FestivalDay>,
) {
    enum class Phase { BEFORE, DURING, AFTER }

    fun phase(now: LocalDateTime): Phase = when {
        now.isBefore(start) -> Phase.BEFORE
        now.isAfter(end) -> Phase.AFTER
        else -> Phase.DURING
    }
}
