package sk.punkacidetom.pd2026.feature.nfctron

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

private const val NFCTRON_URL     = "https://davidbadin.github.io/PD2026_app/pd_resources/nfctron.html"
private const val CACHE_FILE      = "nfctron_cache.html"
private const val ASSET_FILE      = "nfctron.html"
private const val ASSET_BASE_URL  = "file:///android_asset/"
private const val REMOTE_BASE_URL = "https://davidbadin.github.io/PD2026_app/pd_resources/"

data class NfctronContent(
    val html: String,
    /** The WebView base URL to use with [html] so relative paths resolve correctly. */
    val baseUrl: String,
)

@HiltViewModel
class NfctronViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
) : ViewModel() {

    private val cacheFile = File(context.filesDir, CACHE_FILE)

    // null = loading, non-null = content to display
    private val _content = MutableStateFlow<NfctronContent?>(null)
    val content: StateFlow<NfctronContent?> = _content

    init {
        loadNfctron()
    }

    private fun loadNfctron() {
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

    private suspend fun readCache(): NfctronContent? = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) NfctronContent(cacheFile.readText(), REMOTE_BASE_URL) else null
    }

    private suspend fun readAsset(): NfctronContent? = withContext(Dispatchers.IO) {
        try {
            val html = context.assets.open(ASSET_FILE).bufferedReader().readText()
            NfctronContent(html, ASSET_BASE_URL)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchFresh() = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(NFCTRON_URL).build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: return@withContext
                    cacheFile.writeText(html)
                    _content.value = NfctronContent(html, REMOTE_BASE_URL)
                }
            }
        } catch (e: Exception) {
            // Background fetch failure — keep showing cached content, no error shown
        }
    }
}
