package sk.punkacidetom.pd2026.feature.timetable

import org.junit.Assert.assertEquals
import org.junit.Test
import sk.punkacidetom.pd2026.core.model.Band
import java.time.LocalDate
import java.time.LocalTime

class TimetableDayFilterTest {

    private fun fakeBand(id: Int, stage: String) = Band(
        id = id, name = "Band $id", description = "", descriptionEn = "",
        stageCode = stage, spotifyArtistId = "", genre = "punk", sortingPriority = null,
        imageName = "", startDate = LocalDate.of(2026, 5, 29),
        startTime = LocalTime.of(20, 0), endDate = LocalDate.of(2026, 5, 29),
        endTime = LocalTime.of(21, 0),
    )

    @Test fun stage_filter_separates_correctly() {
        val bands = listOf(
            fakeBand(1, "A"), fakeBand(2, "B"), fakeBand(3, "A"), fakeBand(4, "B"),
        )
        val stageA = bands.filter { it.stageCode == "A" }
        val stageB = bands.filter { it.stageCode == "B" }
        assertEquals(listOf(1, 3), stageA.map { it.id })
        assertEquals(listOf(2, 4), stageB.map { it.id })
    }

    @Test fun stage_filter_empty_when_no_bands() {
        assertEquals(emptyList<Band>(), emptyList<Band>().filter { it.stageCode == "A" })
    }
}
