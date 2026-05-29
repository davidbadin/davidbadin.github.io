package sk.punkacidetom.pd2026.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val LANGUAGE_KEY = stringPreferencesKey("language")
private val FONT_LARGE_KEY = booleanPreferencesKey("font_large")
private val FAVOURITE_IDS_KEY = stringSetPreferencesKey("favourite_ids")

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val language: Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "sk" }
    val isFontLarge: Flow<Boolean> = dataStore.data.map { it[FONT_LARGE_KEY] ?: false }
    val favouriteIds: Flow<Set<Int>> = dataStore.data.map { prefs ->
        prefs[FAVOURITE_IDS_KEY]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
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
}
