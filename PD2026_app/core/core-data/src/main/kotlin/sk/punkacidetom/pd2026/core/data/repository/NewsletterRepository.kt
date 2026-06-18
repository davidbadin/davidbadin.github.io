package sk.punkacidetom.pd2026.core.data.repository

import kotlinx.coroutines.flow.Flow
import sk.punkacidetom.pd2026.core.model.NewsletterVolume

interface NewsletterRepository {
    fun observeVolumes(): Flow<List<NewsletterVolume>>
    fun observeAnyPublished(): Flow<Boolean>
    suspend fun refreshManifest(): Result<Unit>
    /** Returns sorted list of cached page file paths for a volume (empty if not yet downloaded). */
    fun cachedPagePaths(volumeId: String): List<String>
    suspend fun downloadVolume(volumeId: String): Result<Unit>
}
