package sk.punkacidetom.pd2026.core.model

import java.time.LocalDate
import java.time.LocalTime

data class Band(
    val id: Int,
    val name: String,
    val description: String,
    val descriptionEn: String,
    val stageCode: String,
    val spotifyArtistId: String,
    val genre: String,
    val sortingPriority: Int?,
    val imageName: String,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val endDate: LocalDate,
    val endTime: LocalTime,
) {
    fun description(locale: String): String =
        if (locale.startsWith("en") && descriptionEn.isNotBlank()) descriptionEn else description

    val bandImagePngUrl: String
        get() = if (imageName.isNotBlank())
            "https://davidbadin.github.io/PD2026_app/bands/$imageName.png"
        else ""

    val bandImageJpgUrl: String
        get() = if (imageName.isNotBlank())
            "https://davidbadin.github.io/PD2026_app/bands/$imageName.jpg"
        else ""

    val spotifyArtistUrl: String
        get() = if (spotifyArtistId.isNotBlank())
            "https://open.spotify.com/artist/$spotifyArtistId"
        else ""
}
