package sk.punkacidetom.pd2026.core.data.util

import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.FestivalDay
import sk.punkacidetom.pd2026.core.model.FestivalInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object FestivalDayCalculator {

    private val DAY_BOUNDARY = LocalTime.of(6, 0)

    /**
     * The festival "day" a band belongs to.
     * A band starting before 06:00 local time belongs to the previous calendar day.
     */
    fun festivalDateFor(bandStartDate: LocalDate, bandStartTime: LocalTime): LocalDate =
        if (bandStartTime.isBefore(DAY_BOUNDARY)) bandStartDate.minusDays(1) else bandStartDate

    /** Build FestivalInfo from the full list of bands. */
    fun compute(bands: List<Band>): FestivalInfo? {
        if (bands.isEmpty()) return null

        val startDt = bands
            .minOf { it.startDate.atTime(it.startTime) }
        val endDt = bands
            .maxOf { it.endDate.atTime(it.endTime) }

        val grouped: Map<LocalDate, List<Band>> = bands
            .groupBy { festivalDateFor(it.startDate, it.startTime) }

        val days = grouped.entries
            .sortedBy { it.key }
            .map { (date, dayBands) ->
                FestivalDay(
                    date = date,
                    bands = dayBands.sortedWith(
                        compareBy({ it.startDate }, { it.startTime })
                    ),
                )
            }

        return FestivalInfo(start = startDt, end = endDt, days = days)
    }

    /** Bands whose slot overlaps [now]. */
    fun nowPlaying(bands: List<Band>, now: LocalDateTime = LocalDateTime.now()): List<Band> =
        bands.filter { band ->
            val startDt = band.startDate.atTime(band.startTime)
            val endDt = band.endDate.atTime(band.endTime)
            !now.isBefore(startDt) && now.isBefore(endDt)
        }
}
