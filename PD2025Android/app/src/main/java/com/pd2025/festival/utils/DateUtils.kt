package com.pd2025.festival.utils

import com.pd2025.festival.model.Constants
import java.util.Calendar
import java.util.Date

object DateUtils {

    /**
     * Parse date string format: "DD.MM.YYYY HH:MM" or "DD.MM.YYYY H:MM"
     * Mirrors formatDate() in tools.js
     */
    fun parseDate(dateStr: String): Date? {
        return try {
            val year = dateStr.substring(6, 10).toInt()
            val month = dateStr.substring(3, 5).toInt() - 1  // 0-based
            val day = dateStr.substring(0, 2).toInt()

            val hour: Int
            val minute: Int
            if (dateStr.length > 12 && dateStr[12] == ':') {
                hour = dateStr.substring(11, 12).toInt()
                minute = dateStr.substring(13, 15).toInt()
            } else {
                hour = dateStr.substring(11, 13).toInt()
                minute = dateStr.substring(14, 16).toInt()
            }

            Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format short description: "HH:MM-HH:MM, Stage Name"
     * Mirrors formatShortDescr() in tools.js
     */
    fun formatShortDescr(stageName: String, startDate: Date, endDate: Date): String {
        val cal = Calendar.getInstance()
        cal.time = startDate
        val startH = cal.get(Calendar.HOUR_OF_DAY)
        val startM = cal.get(Calendar.MINUTE)

        cal.time = endDate
        val endH = cal.get(Calendar.HOUR_OF_DAY)
        val endM = cal.get(Calendar.MINUTE)

        val startStr = "$startH:${startM.toString().padStart(2, '0')}"
        val endStr = "$endH:${endM.toString().padStart(2, '0')}"
        return "$startStr-$endStr, $stageName"
    }

    /**
     * Get start datetime of the given festival day
     * Mirrors getCurrentDayStart() in tools.js
     */
    fun getDayStart(dayNumber: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = Constants.FESTIVAL_START.time
        cal.set(Calendar.HOUR_OF_DAY, Constants.SCHED_START_HOUR)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_MONTH, dayNumber - 1)
        return cal.time
    }

    /**
     * Get end datetime of the given festival day
     * Mirrors getCurrentDayEnd() in tools.js
     */
    fun getDayEnd(dayNumber: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = Constants.FESTIVAL_START.time
        cal.set(Calendar.HOUR_OF_DAY, Constants.SCHED_END_HOUR)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        // end hour is after midnight → add extra day
        val plusDays = if (Constants.SCHED_START_HOUR > Constants.SCHED_END_HOUR) {
            dayNumber
        } else {
            dayNumber - 1
        }
        cal.add(Calendar.DAY_OF_MONTH, plusDays)
        return cal.time
    }

    /**
     * Format hour for display (handle 24h rollover)
     */
    fun displayHour(hour: Int): String {
        val h = if (hour >= 24) hour - 24 else hour
        return h.toString()
    }
}
