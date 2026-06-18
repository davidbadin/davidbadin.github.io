package sk.punkacidetom.pd2026.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import sk.punkacidetom.pd2026.core.model.NewsletterVolume
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private const val MANIFEST_URL =
    "https://davidbadin.github.io/PD2026_app/pd_resources/news/newsletter_manifest.json"

@Singleton
class NewsletterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
) : NewsletterRepository {

    private val rootDir = File(context.filesDir, "newsletter").also { it.mkdirs() }
    private val manifestFile = File(rootDir, "manifest.json")
    private val _volumes = MutableStateFlow<List<NewsletterVolume>>(emptyList())

    init {
        if (manifestFile.exists()) {
            _volumes.value = parseManifest(manifestFile.readText())
        }
    }

    override fun observeVolumes(): Flow<List<NewsletterVolume>> = _volumes

    override fun observeAnyPublished(): Flow<Boolean> =
        _volumes.map { list -> list.any { it.isPublished() } }

    override suspend fun refreshManifest(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(MANIFEST_URL).build()
            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                val newJson = response.body?.string() ?: error("Empty body")
                val newVolumes = parseManifest(newJson)

                // Invalidate cached PNGs for any volume whose publishAt changed
                val oldMap = _volumes.value.associateBy { it.id }
                newVolumes.forEach { newVol ->
                    val old = oldMap[newVol.id]
                    if (old != null && old.publishAt != newVol.publishAt) {
                        File(rootDir, newVol.id).deleteRecursively()
                    }
                }

                manifestFile.writeText(newJson)
                _volumes.value = newVolumes
            }
        }
    }

    override fun cachedPagePaths(volumeId: String): List<String> {
        val dir = File(rootDir, volumeId)
        if (!dir.exists()) return emptyList()
        return dir.listFiles()
            ?.filter { it.extension == "png" }
            ?.sortedBy { it.name }
            ?.map { it.absolutePath }
            ?: emptyList()
    }

    override suspend fun downloadVolume(volumeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val volume = _volumes.value.find { it.id == volumeId }
                ?: error("Volume $volumeId not found")
            check(volume.isPublished()) { "Volume $volumeId not yet published" }

            File(rootDir, volumeId).mkdirs()
            // Probe pages 01.png, 02.png, … until a non-200 response (no pageCount needed)
            var page = 1
            while (true) {
                val dest = pageFile(volumeId, page)
                if (!dest.exists()) {
                    val req = Request.Builder().url(volume.pageUrl(page)).build()
                    val resp = httpClient.newCall(req).execute()
                    if (!resp.isSuccessful) break   // 404 or other = no more pages
                    dest.writeBytes(resp.body!!.bytes())
                    resp.close()
                }
                page++
            }
        }
    }

    private fun pageFile(volumeId: String, page: Int): File =
        File(File(rootDir, volumeId), "${page.toString().padStart(2, '0')}.png")

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
