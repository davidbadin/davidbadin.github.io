package com.davidbadin.kanaread.data

import android.content.Context

/**
 * Persists the user's personal-best average-time per practice mode.
 *
 * Records are stored in SharedPreferences (one float per mode).
 * A "best" is only saved if a session ended with at least
 * [MIN_CORRECT] correct answers AND the new average time is faster
 * (smaller) than the current record.
 *
 * Three modes are tracked: "hiragana", "katakana", "both".
 */
class BestRecordsRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * Returns the best avg-time-per-correct-word in seconds, or null
     * if no record exists yet for this mode.
     */
    fun getBest(mode: String): Float? {
        val v = prefs.getFloat(keyFor(mode), -1f)
        return if (v < 0f) null else v
    }

    /**
     * Save the avg time as the new best for [mode] if it qualifies.
     * Returns true when a new record was actually written.
     */
    fun saveIfBest(mode: String, avgTimeSeconds: Float, correctCount: Int): Boolean {
        if (correctCount < MIN_CORRECT) return false
        if (avgTimeSeconds <= 0f) return false
        val current = getBest(mode)
        if (current == null || avgTimeSeconds < current) {
            prefs.edit().putFloat(keyFor(mode), avgTimeSeconds).apply()
            return true
        }
        return false
    }

    private fun keyFor(mode: String) = "best_avg_$mode"

    companion object {
        private const val PREFS = "kanaread_prefs"
        const val MIN_CORRECT = 10
    }
}
