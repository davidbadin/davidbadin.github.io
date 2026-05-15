package com.davidbadin.kanaread.data

import android.content.Context

/**
 * Three-state theme preference:
 *  - SYSTEM: follow the device setting (default)
 *  - LIGHT:  always light
 *  - DARK:   always dark
 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Persists the user's theme choice in SharedPreferences.
 */
class ThemeRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getMode(): ThemeMode {
        val raw = prefs.getString(KEY, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY, mode.name).apply()
    }

    /**
     * Convenience: returns the next mode in the cycle
     * SYSTEM -> LIGHT -> DARK -> SYSTEM.
     */
    fun cycle(current: ThemeMode): ThemeMode = when (current) {
        ThemeMode.SYSTEM -> ThemeMode.LIGHT
        ThemeMode.LIGHT -> ThemeMode.DARK
        ThemeMode.DARK -> ThemeMode.SYSTEM
    }

    companion object {
        private const val PREFS = "kanaread_prefs"
        private const val KEY = "theme_mode"
    }
}
