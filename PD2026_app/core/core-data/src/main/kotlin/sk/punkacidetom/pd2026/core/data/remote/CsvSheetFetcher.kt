package sk.punkacidetom.pd2026.core.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvSheetFetcher @Inject constructor(
    private val client: OkHttpClient,
    private val sheetId: String,
    private val sheetGid: String,
) {
    private val url: String
        get() = "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv&gid=$sheetGid"

    suspend fun fetchCsv(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code} fetching sheet")
            response.body?.string() ?: error("Empty response body")
        }
    }
}
