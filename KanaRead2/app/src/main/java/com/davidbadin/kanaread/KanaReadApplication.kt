package com.davidbadin.kanaread

import android.app.Application
import com.davidbadin.kanaread.data.BestRecordsRepository
import com.davidbadin.kanaread.data.KanaDatabase
import com.davidbadin.kanaread.data.ThemeRepository

/**
 * Application class — entry point for process-wide setup.
 * Holds the singleton Room database, the SharedPreferences-backed
 * personal-best repository, and the theme-mode repository. Seeding
 * happens later in MainActivity so we can show a loading indicator
 * and run it on a coroutine.
 */
class KanaReadApplication : Application() {

    val database: KanaDatabase by lazy { KanaDatabase.getInstance(this) }

    val bestRecords: BestRecordsRepository by lazy { BestRecordsRepository(this) }

    val themeRepository: ThemeRepository by lazy { ThemeRepository(this) }
}
