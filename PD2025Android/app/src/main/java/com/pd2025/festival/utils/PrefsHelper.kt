package com.pd2025.festival.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pd2025.festival.model.Event

object PrefsHelper {

    private const val PREFS_NAME = "pd2025_prefs"
    private const val KEY_EVENTS = "pd2025_data"
    private val gson = Gson()

    fun saveEvents(context: Context, events: List<Event>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_EVENTS, gson.toJson(events)).apply()
    }

    fun loadEvents(context: Context): List<Event>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_EVENTS, null) ?: return null
        return try {
            val type = object : TypeToken<List<Event>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun saveFavorites(context: Context, favorites: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet("favorites", favorites).apply()
    }

    fun loadFavorites(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }
}
