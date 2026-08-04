package sk.punkacidetom.pd2026.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import sk.punkacidetom.pd2026.core.model.NewsletterVolume
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsletterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NewsletterRepository {

    private val _volumes = MutableStateFlow<List<NewsletterVolume>>(emptyList())

    init {
        try {
            val json = context.assets.open("news/newsletter_manifest.json")
                .bufferedReader().use { it.readText() }
            _volumes.value = parseManifest(json)
        } catch (_: Exception) { /* stay empty */ }
    }

    override fun observeVolumes(): Flow<List<NewsletterVolume>> = _volumes

    override fun observeAnyPublished(): Flow<Boolean> =
        _volumes.map { list -> list.any { it.isPublished() } }

    override suspend fun refreshManifest(): Result<Unit> = Result.success(Unit)

    override suspend fun downloadVolume(volumeId: String): Result<Unit> = Result.success(Unit)

    override fun cachedPagePaths(volumeId: String): List<String> =
        (context.assets.list("news/$volumeId") ?: emptyArray())
            .filter { it.endsWith(".png") }
            .sorted()
            .map { "file:///android_asset/news/$volumeId/$it" }

    private fun parseManifest(json: String): List<NewsletterVolume> {
        val arr = JSONObject(json).getJSONArray("volumes")
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            NewsletterVolume(
                id = obj.getString("id"),
                publishAt = LocalDateTime.parse(obj.getString("publishAt")),
            )
        }
    }
}
