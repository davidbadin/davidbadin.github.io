package sk.punkacidetom.pd2026.core.data.mapper

import sk.punkacidetom.pd2026.core.model.Band
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Maps a parsed CSV (list of rows) into a list of Band domain objects.
 *
 * Sheet schema (spec §3.2):
 *   A=ID  B=START_DATE  C=START_TIME  D=END_DATE  E=END_TIME
 *   F=BAND  G=DESCRIPTION  H=STAGE  I=SPOTIFY_URL  J=GENRE
 *   K=SORTING_PRIORITY  L=DESCRIPTION_EN  M=IMAGE_NAME
 *
 * Rules:
 *  - Row 1 is the header row; it is used to locate columns by name.
 *  - A blank ID means "ignore this row" — silently skipped.
 *  - Duplicate IDs: keep only the first occurrence.
 *  - Missing columns (e.g. DESCRIPTION_EN not yet added) are treated as empty strings.
 */
object BandMapper {

    private val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("H:mm:ss")

    fun mapRows(rows: List<List<String>>): List<Band> {
        if (rows.isEmpty()) return emptyList()

        val header = rows.first().map { it.trim().uppercase() }
        val col = { name: String -> header.indexOf(name).takeIf { it >= 0 } }

        val idCol = col("ID") ?: return emptyList()
        val startDateCol = col("START_DATE") ?: return emptyList()
        val startTimeCol = col("START_TIME") ?: return emptyList()
        val endDateCol = col("END_DATE") ?: return emptyList()
        val endTimeCol = col("END_TIME") ?: return emptyList()
        val bandCol = col("BAND") ?: return emptyList()
        val descCol = col("DESCRIPTION")
        val stageCol = col("STAGE") ?: return emptyList()
        val spotifyCol = col("SPOTIFY_URL")
        val genreCol = col("GENRE")
        val priorityCol = col("SORTING_PRIORITY")
        val descEnCol = col("DESCRIPTION_EN")
        val imageCol = col("IMAGE_NAME")

        val seenIds = mutableSetOf<Int>()

        return rows.drop(1).mapNotNull { row ->
            fun field(colIdx: Int?): String = colIdx?.let { row.getOrNull(it)?.trim() } ?: ""

            val idStr = field(idCol)
            if (idStr.isBlank()) return@mapNotNull null

            val id = idStr.toIntOrNull() ?: return@mapNotNull null
            if (!seenIds.add(id)) return@mapNotNull null

            val startDate = parseDate(field(startDateCol)) ?: return@mapNotNull null
            val startTime = parseTime(field(startTimeCol)) ?: return@mapNotNull null
            val endDate = parseDate(field(endDateCol)) ?: return@mapNotNull null
            val endTime = parseTime(field(endTimeCol)) ?: return@mapNotNull null

            Band(
                id = id,
                name = field(bandCol),
                description = field(descCol),
                descriptionEn = field(descEnCol),
                stageCode = field(stageCol),
                spotifyArtistId = field(spotifyCol),
                genre = field(genreCol),
                sortingPriority = field(priorityCol).toIntOrNull(),
                imageName = field(imageCol),
                startDate = startDate,
                startTime = startTime,
                endDate = endDate,
                endTime = endTime,
            )
        }
    }

    private fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value, dateFormatter)
    } catch (e: DateTimeParseException) {
        null
    }

    private fun parseTime(value: String): LocalTime? = try {
        LocalTime.parse(value, timeFormatter)
    } catch (e: DateTimeParseException) {
        null
    }
}
