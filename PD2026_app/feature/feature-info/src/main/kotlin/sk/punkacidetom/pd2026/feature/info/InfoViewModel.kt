package sk.punkacidetom.pd2026.feature.info

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

private const val INFO_URL = "https://davidbadin.github.io/PD2026_app/info.html"
private const val CACHE_FILE = "info_cache.html"
private const val ASSET_FILE = "info.html"

/**
 * Base URL used when serving the bundled [ASSET_FILE].
 * All relative paths in info.html (logo, fonts) resolve to android_asset/.
 */
private const val ASSET_BASE_URL = "file:///android_asset/"

/**
 * Base URL used when serving content fetched from the network / cache.
 * Relative paths in the server-supplied HTML resolve against GitHub Pages.
 */
private const val REMOTE_BASE_URL = "https://davidbadin.github.io/PD2026_app/"

data class InfoContent(
    val html: String,
    /** The WebView base URL to use with [html] so relative paths resolve correctly. */
    val baseUrl: String,
)

@HiltViewModel
class InfoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
) : ViewModel() {

    private val cacheFile = File(context.filesDir, CACHE_FILE)

    // null = loading, non-null = content to display
    private val _content = MutableStateFlow<InfoContent?>(null)
    val content: StateFlow<InfoContent?> = _content

    init {
        loadInfo()
    }

    private fun loadInfo() {
        viewModelScope.launch {
            // 1. Show cached file if available (uses remote base URL so relative
            //    paths in the server-supplied HTML resolve correctly).
            //    Fall back to the bundled asset, which uses the local base URL.
            val initial = readCache() ?: readAsset()
            _content.value = initial

            // 2. Fetch fresh HTML in background (stale-while-revalidate)
            fetchFresh()
        }
    }

    private suspend fun readCache(): InfoContent? = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) InfoContent(cacheFile.readText(), REMOTE_BASE_URL) else null
    }

    private suspend fun readAsset(): InfoContent? = withContext(Dispatchers.IO) {
        try {
            val html = context.assets.open(ASSET_FILE).bufferedReader().readText()
            InfoContent(html, ASSET_BASE_URL)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchFresh() = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(INFO_URL).build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: return@withContext
                    cacheFile.writeText(html)
                    _content.value = InfoContent(html, REMOTE_BASE_URL)
                }
            }
        } catch (e: Exception) {
            // Background fetch failure — keep showing cached content, no error shown
        }
    }
}
