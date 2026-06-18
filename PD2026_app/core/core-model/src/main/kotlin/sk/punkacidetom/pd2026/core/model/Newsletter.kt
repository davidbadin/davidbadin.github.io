package sk.punkacidetom.pd2026.core.model

import java.time.LocalDateTime

data class NewsletterVolume(
    val id: String,
    val publishAt: LocalDateTime,
) {
    fun isPublished(now: LocalDateTime = LocalDateTime.now()): Boolean = !now.isBefore(publishAt)

    fun pageUrl(page: Int): String =
        "https://davidbadin.github.io/PD2026_app/pd_resources/news/$id/${page.toString().padStart(2, '0')}.png"
}
