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

@HiltViewModel
class InfoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
) : ViewModel() {

    private val cacheFile = File(context.filesDir, CACHE_FILE)

    // null = loading, non-null = HTML string to display
    private val _htmlContent = MutableStateFlow<String?>(null)
    val htmlContent: StateFlow<String?> = _htmlContent

    init {
        loadInfo()
    }

    private fun loadInfo() {
        viewModelScope.launch {
            // 1. Show cached file if available, otherwise fall back to bundled asset
            val initial = readCache() ?: readAsset()
            _htmlContent.value = initial

            // 2. Fetch fresh HTML in background (stale-while-revalidate)
            fetchFresh()
        }
    }

    private suspend fun readCache(): String? = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) cacheFile.readText() else null
    }

    private suspend fun readAsset(): String? = withContext(Dispatchers.IO) {
        try {
            context.assets.open(ASSET_FILE).bufferedReader().readText()
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
                    _htmlContent.value = html
                }
            }
        } catch (e: Exception) {
            // Background fetch failure — keep showing cached content, no error shown
        }
    }
}
