package sk.punkacidetom.pd2026.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.cache.BandCache
import sk.punkacidetom.pd2026.core.data.mapper.BandMapper
import sk.punkacidetom.pd2026.core.data.remote.CsvParser
import sk.punkacidetom.pd2026.core.data.remote.CsvSheetFetcher
import sk.punkacidetom.pd2026.core.data.remote.XlsxAssetReader
import sk.punkacidetom.pd2026.core.data.util.FestivalDayCalculator
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.FestivalInfo
import javax.inject.Inject
import javax.inject.Singleton

private val LAST_FETCH_KEY = longPreferencesKey("last_fetch_timestamp_ms")
private const val REFRESH_COOLDOWN_MS = 30 * 60 * 1000L // 30 minutes

@Singleton
class BandRepositoryImpl @Inject constructor(
    private val fetcher: CsvSheetFetcher,
    private val cache: BandCache,
    private val dataStore: DataStore<Preferences>,
    private val xlsxReader: XlsxAssetReader,
    private val newsletterRepository: NewsletterRepository,
) : BandRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _bands = MutableStateFlow<List<Band>>(emptyList())

    init {
        scope.launch {
            // Load cache immediately on init
            val cached = cache.loadBands()
            if (cached.isNotEmpty()) _bands.value = cached

            // App start always fetches fresh data regardless of cooldown
            backgroundRefreshIfStale(ignoreCooldown = true)
        }
    }

    override fun observeBands(): Flow<List<Band>> = _bands.asStateFlow()

    override fun observeFestivalInfo(): Flow<FestivalInfo?> =
        _bands.map { FestivalDayCalculator.compute(it) }

    override fun observeSortedBands(): Flow<List<Band>> = _bands.map { bands ->
        bands.sortedWith(
            compareBy<Band> { it.sortingPriority ?: Int.MAX_VALUE }
                .thenBy { it.name }
        )
    }

    override suspend fun forceRefresh(): Result<Unit> = doFetch()

    /** Called on app resume — refreshes only if >30 min since last successful fetch. */
    suspend fun refreshIfStale() = backgroundRefreshIfStale(ignoreCooldown = false)

    private fun backgroundRefreshIfStale(ignoreCooldown: Boolean) {
        scope.launch { doFetchIfStale(ignoreCooldown) }
    }

    private suspend fun doFetchIfStale(ignoreCooldown: Boolean) {
        if (!ignoreCooldown) {
            val lastFetch = dataStore.data.first()[LAST_FETCH_KEY] ?: 0L
            val age = System.currentTimeMillis() - lastFetch
            if (age < REFRESH_COOLDOWN_MS) return
        }
        doFetch()
    }

    private suspend fun doFetch(): Result<Unit> = runCatching {
        val csv = try {
            fetcher.fetchCsv()
        } catch (e: Exception) {
            // Network unavailable — fall back to bundled XLSX only if no data loaded yet
            if (_bands.value.isEmpty()) {
                val rows = xlsxReader.readRows()
                if (rows != null) {
                    val bands = BandMapper.mapRows(rows)
                    if (bands.isNotEmpty()) {
                        _bands.value = bands
                        // Do NOT persist to cache or update timestamp — this is test-only data
                    }
                }
            }
            throw e   // re-throw so callers know the network fetch failed
        }
        val rows = CsvParser.parse(csv)
        val bands = BandMapper.mapRows(rows)
        _bands.value = bands
        cache.saveBands(bands)
        dataStore.edit { it[LAST_FETCH_KEY] = System.currentTimeMillis() }

        // Network is confirmed reachable — refresh newsletter manifest concurrently
        scope.launch { newsletterRepository.refreshManifest() }
    }
}
