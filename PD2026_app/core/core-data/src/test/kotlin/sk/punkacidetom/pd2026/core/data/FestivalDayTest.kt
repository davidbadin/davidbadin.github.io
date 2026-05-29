package sk.punkacidetom.pd2026.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import sk.punkacidetom.pd2026.core.data.util.FestivalDayCalculator
import sk.punkacidetom.pd2026.core.model.Band
import java.time.LocalDate
import java.time.LocalTime

class FestivalDayTest {

    private fun makeBand(id: Int, startDate: LocalDate, startTime: LocalTime, endDate: LocalDate = startDate, endTime: LocalTime = startTime.plusHours(1)) = Band(
        id = id, name = "Band$id", description = "", descriptionEn = "", stageCode = "A",
        spotifyArtistId = "", genre = "", sortingPriority = null, imageName = "",
        startDate = startDate, startTime = startTime, endDate = endDate, endTime = endTime,
    )

    private val friday = LocalDate.of(2026, 5, 29)
    private val saturday = LocalDate.of(2026, 5, 30)

    @Test
    fun `band at 20h on Friday belongs to Friday`() {
        val date = FestivalDayCalculator.festivalDateFor(friday, LocalTime.of(20, 0))
        assertEquals(friday, date)
    }

    @Test
    fun `band at 06h00 on Saturday belongs to Saturday`() {
        val date = FestivalDayCalculator.festivalDateFor(saturday, LocalTime.of(6, 0))
        assertEquals(saturday, date)
    }

    @Test
    fun `band at 03h00 on Saturday belongs to Friday`() {
        val date = FestivalDayCalculator.festivalDateFor(saturday, LocalTime.of(3, 0))
        assertEquals(friday, date)
    }

    @Test
    fun `band at 05h59 on Saturday belongs to Friday`() {
        val date = FestivalDayCalculator.festivalDateFor(saturday, LocalTime.of(5, 59))
        assertEquals(friday, date)
    }

    @Test
    fun `compute returns null for empty list`() {
        assertNull(FestivalDayCalculator.compute(emptyList()))
    }

    @Test
    fun `compute derives correct start and end`() {
        val bands = listOf(
            makeBand(1, friday, LocalTime.of(18, 0), friday, LocalTime.of(19, 0)),
            makeBand(2, saturday, LocalTime.of(2, 0), saturday, LocalTime.of(3, 0)),
            makeBand(3, saturday, LocalTime.of(22, 0), saturday, LocalTime.of(23, 0)),
        )
        val info = FestivalDayCalculator.compute(bands)
        assertNotNull(info)
        assertEquals(friday.atTime(LocalTime.of(18, 0)), info!!.start)
        assertEquals(saturday.atTime(LocalTime.of(23, 0)), info.end)
    }

    @Test
    fun `band at 03h Saturday grouped into Friday festival day`() {
        val bands = listOf(
            makeBand(1, friday, LocalTime.of(20, 0)),
            makeBand(2, saturday, LocalTime.of(3, 0)), // late night — belongs to Friday
            makeBand(3, saturday, LocalTime.of(20, 0)),
        )
        val info = FestivalDayCalculator.compute(bands)!!
        assertEquals(2, info.days.size)
        val fridayDay = info.days.first { it.date == friday }
        assertEquals(2, fridayDay.bands.size) // band 1 + late-night band 2
        val saturdayDay = info.days.first { it.date == saturday }
        assertEquals(1, saturdayDay.bands.size) // band 3
    }

    @Test
    fun `nowPlaying returns only current bands`() {
        val now = friday.atTime(20, 30)
        val bands = listOf(
            makeBand(1, friday, LocalTime.of(20, 0), friday, LocalTime.of(21, 0)), // playing
            makeBand(2, friday, LocalTime.of(21, 0), friday, LocalTime.of(22, 0)), // not yet
            makeBand(3, friday, LocalTime.of(19, 0), friday, LocalTime.of(20, 0)), // over
        )
        val playing = FestivalDayCalculator.nowPlaying(bands, now)
        assertEquals(1, playing.size)
        assertEquals(1, playing[0].id)
    }
}
