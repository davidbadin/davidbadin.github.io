package sk.punkacidetom.pd2026.feature.bands

import org.junit.Assert.assertEquals
import org.junit.Test
import sk.punkacidetom.pd2026.core.model.Band
import java.time.LocalDate
import java.time.LocalTime

class BandSortTest {

    private fun band(id: Int, name: String, priority: Int?) = Band(
        id = id, name = name, description = "", descriptionEn = "",
        stageCode = "A", spotifyArtistId = "", genre = "", sortingPriority = priority,
        imageName = "", startDate = LocalDate.of(2026, 5, 29),
        startTime = LocalTime.of(20, 0), endDate = LocalDate.of(2026, 5, 29),
        endTime = LocalTime.of(21, 0),
    )

    private fun sort(bands: List<Band>): List<Band> =
        bands.sortedWith(compareBy<Band> { it.sortingPriority }.thenBy { it.name })
            .sortedWith(Comparator { a, b ->
                when {
                    a.sortingPriority == null && b.sortingPriority == null -> a.name.compareTo(b.name)
                    a.sortingPriority == null -> 1
                    b.sortingPriority == null -> -1
                    a.sortingPriority != b.sortingPriority -> a.sortingPriority.compareTo(b.sortingPriority)
                    else -> a.name.compareTo(b.name)
                }
            })

    @Test fun priority_ascending_nulls_last() {
        val bands = listOf(
            band(1, "Zebra", null),
            band(2, "Alpha", 2),
            band(3, "Beta", 1),
        )
        val sorted = sort(bands)
        assertEquals(listOf(3, 2, 1), sorted.map { it.id })
    }

    @Test fun tie_break_by_name() {
        val bands = listOf(
            band(1, "Zebra", 1),
            band(2, "Alpha", 1),
        )
        val sorted = sort(bands)
        assertEquals(listOf(2, 1), sorted.map { it.id })
    }
}
