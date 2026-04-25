package com.pd2025.festival.model

import java.util.Calendar

object Constants {
    const val TITLE_LONG = "Punkáči deťom 2025"
    const val TITLE_SHORT = "PD2025"

    const val SCHED_START_HOUR = 12
    const val SCHED_END_HOUR = 2   // 2am next day

    val FESTIVAL_START: Calendar = Calendar.getInstance().apply {
        set(2025, Calendar.AUGUST, 28, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val DAYS = listOf(
        FestivalDay("štvrtok", 1),
        FestivalDay("piatok", 2),
        FestivalDay("sobota", 3)
    )

    val STAGES = listOf(
        Stage("Punk For Children Stage", "A"),
        Stage("United Stage", "B")
    )

    const val API_URI = "https://sheets.googleapis.com/v4/spreadsheets/"
    const val SHEET_ID = "1VvLnbTEg61lK6h6aib0O8wTEuWpGaYieLCCQ4qfUg5U"
    const val API_KEY = "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI"
    const val SHEET_RANGE = "'PD2025'!A2:H100"

    const val SPOTIFY_PLAYLIST = "https://open.spotify.com/playlist/6xLDXTKkGWlNS2Ky2enHuI"
    const val LOCAL_STORAGE_KEY = "pd2025_data"

    // Total hours in schedule (12:00 to 02:00 next day = 14 hours)
    val TOTAL_HOURS: Int get() {
        val diff = SCHED_END_HOUR - SCHED_START_HOUR
        return if (diff <= 0) diff + 24 else diff
    }
}
