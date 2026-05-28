package sk.punkacidetom.pd2026.core.data

import org.junit.Assert.assertEquals
import org.junit.Test
import sk.punkacidetom.pd2026.core.model.Band
import java.time.LocalDate
import java.time.LocalTime

class SortOrderTest {

    private fun band(id: Int, name: String, priority: Int?) = Band(
        id = id, name = name, description = "", descriptionEn = "", stageCode = "A",
        spotifyArtistId = "", genre = "", sortingPriority = priority, imageName = "",
        startDate = LocalDate.of(2026, 5, 29), startTime = LocalTime.of(20, 0),
        endDate = LocalDate.of(2026, 5, 29), endTime = LocalTime.of(21, 0),
    )

    private fun sortedNames(bands: List<Band>): List<String> = bands
        .sortedWith(
            compareBy<Band> { it.sortingPriority ?: Int.MAX_VALUE }.thenBy { it.name }
        )
        .map { it.name }

    @Test
    fun `priority ascending, nulls at bottom, then name ascending`() {
        val bands = listOf(
            band(1, "Charlie", 3),
            band(2, "Alpha", null),
            band(3, "Beta", 1),
            band(4, "Delta", 2),
            band(5, "Echo", null),
        )
        val names = sortedNames(bands)
        assertEquals(listOf("Beta", "Delta", "Charlie", "Alpha", "Echo"), names)
    }

    @Test
    fun `same priority broken by name`() {
        val bands = listOf(
            band(1, "Zeta", 1),
            band(2, "Alpha", 1),
            band(3, "Mango", 1),
        )
        val names = sortedNames(bands)
        assertEquals(listOf("Alpha", "Mango", "Zeta"), names)
    }

    @Test
    fun `all null priorities sorted by name`() {
        val bands = listOf(
            band(1, "Zap", null),
            band(2, "Ant", null),
            band(3, "Man", null),
        )
        val names = sortedNames(bands)
        assertEquals(listOf("Ant", "Man", "Zap"), names)
    }
}
