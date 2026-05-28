package sk.punkacidetom.pd2026.core.model

import java.time.LocalDate

/**
 * A festival "day" is the window from 06:00 on [date] to 05:59 the next day.
 * A band playing at 03:00 on Saturday belongs to the Friday festival day.
 */
data class FestivalDay(
    val date: LocalDate,
    val bands: List<Band>,
)
