package sk.punkacidetom.pd2026.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val LANGUAGE_KEY              = stringPreferencesKey("language")
private val FONT_LARGE_KEY            = booleanPreferencesKey("font_large")
private val FAVOURITE_IDS_KEY         = stringSetPreferencesKey("favourite_ids")
private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
private val EXACT_ALARM_MISSING_KEY   = booleanPreferencesKey("exact_alarm_permission_missing")
// Encodes Map<Int, Long> as Set<"bandId:epochMs"> — DataStore has no native map type
private val SCHEDULED_NOTIFICATIONS_KEY  = stringSetPreferencesKey("scheduled_band_notifications")
private val EXACT_ALARM_DIALOG_SHOWN_KEY = booleanPreferencesKey("exact_alarm_dialog_shown")

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val language: Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "sk" }
    val isFontLarge: Flow<Boolean> = dataStore.data.map { it[FONT_LARGE_KEY] ?: false }
    val favouriteIds: Flow<Set<Int>> = dataStore.data.map { prefs ->
        prefs[FAVOURITE_IDS_KEY]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map {
        it[NOTIFICATIONS_ENABLED_KEY] ?: true
    }
    val exactAlarmPermissionMissing: Flow<Boolean> = dataStore.data.map {
        it[EXACT_ALARM_MISSING_KEY] ?: false
    }
    /** `true` once the one-time exact-alarm rationale dialog has been shown. Never reset. */
    val exactAlarmDialogShown: Flow<Boolean> = dataStore.data.map {
        it[EXACT_ALARM_DIALOG_SHOWN_KEY] ?: false
    }

    /**
     * Seeds the language preference from the device locale on first launch only.
     * If a value already exists (user has made a choice), this is a no-op.
     * @param systemLanguage  Locale.getDefault().language — e.g. "sk", "en", "de"
     */
    suspend fun initLanguageIfAbsent(systemLanguage: String) {
        dataStore.edit { prefs ->
            if (!prefs.contains(LANGUAGE_KEY)) {
                prefs[LANGUAGE_KEY] = if (systemLanguage == "sk") "sk" else "en"
            }
        }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[LANGUAGE_KEY] = lang }
    }

    suspend fun setFontLarge(large: Boolean) {
        dataStore.edit { it[FONT_LARGE_KEY] = large }
    }

    suspend fun toggleFavourite(bandId: Int) {
        dataStore.edit { prefs ->
            val current = prefs[FAVOURITE_IDS_KEY] ?: emptySet()
            val idStr = bandId.toString()
            prefs[FAVOURITE_IDS_KEY] = if (idStr in current) current - idStr else current + idStr
        }
    }

    suspend fun isFavourite(bandId: Int): Boolean {
        val current = dataStore.data.map { it[FAVOURITE_IDS_KEY] ?: emptySet() }
        return current.map { it.contains(bandId.toString()) }.let { flow ->
            var result = false
            flow.collect { result = it; return@collect }
            result
        }
    }

    // -------------------------------------------------------------------------
    // Notifications enabled
    // -------------------------------------------------------------------------

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    // -------------------------------------------------------------------------
    // Exact-alarm permission flag (API 31+)
    // -------------------------------------------------------------------------

    suspend fun setExactAlarmPermissionMissing(missing: Boolean) {
        dataStore.edit { it[EXACT_ALARM_MISSING_KEY] = missing }
    }

    /** Marks the one-time exact-alarm rationale dialog as shown. Idempotent. */
    suspend fun setExactAlarmDialogShown() {
        dataStore.edit { it[EXACT_ALARM_DIALOG_SHOWN_KEY] = true }
    }

    // -------------------------------------------------------------------------
    // Scheduled-notification tracking map: bandId → alarm fire time in epoch-ms
    // -------------------------------------------------------------------------

    /** Returns the currently scheduled alarms as a map of bandId → triggerAtMs. */
    suspend fun getScheduledNotifications(): Map<Int, Long> =
        dataStore.data.first()[SCHEDULED_NOTIFICATIONS_KEY]?.toScheduledMap() ?: emptyMap()

    /** Adds or replaces the entry for [bandId]. */
    suspend fun saveScheduledNotification(bandId: Int, epochMs: Long) {
        dataStore.edit { prefs ->
            val current = prefs[SCHEDULED_NOTIFICATIONS_KEY]?.toScheduledMap()?.toMutableMap()
                ?: mutableMapOf()
            current[bandId] = epochMs
            prefs[SCHEDULED_NOTIFICATIONS_KEY] = current.toEncodedSet()
        }
    }

    /** Removes the entry for [bandId] (no-op if absent). */
    suspend fun removeScheduledNotification(bandId: Int) {
        dataStore.edit { prefs ->
            val current = prefs[SCHEDULED_NOTIFICATIONS_KEY]?.toScheduledMap()?.toMutableMap()
                ?: return@edit
            current.remove(bandId)
            prefs[SCHEDULED_NOTIFICATIONS_KEY] = current.toEncodedSet()
        }
    }

    /** Clears the entire scheduled-notifications map. */
    suspend fun clearScheduledNotifications() {
        dataStore.edit { it[SCHEDULED_NOTIFICATIONS_KEY] = emptySet() }
    }
}

// -------------------------------------------------------------------------
// Encoding helpers
// -------------------------------------------------------------------------

private fun Set<String>.toScheduledMap(): Map<Int, Long> =
    mapNotNull { entry ->
        val idx = entry.indexOf(':')
        if (idx <= 0) return@mapNotNull null
        val id  = entry.substring(0, idx).toIntOrNull()  ?: return@mapNotNull null
        val ms  = entry.substring(idx + 1).toLongOrNull() ?: return@mapNotNull null
        id to ms
    }.toMap()

private fun Map<Int, Long>.toEncodedSet(): Set<String> =
    entries.map { "${it.key}:${it.value}" }.toSet()
