package com.davidbadin.kanaread

import android.app.Application
import com.davidbadin.kanaread.data.KanaDatabase

/**
 * Application class — entry point for process-wide setup.
 * Holds the singleton Room database. Seeding happens later in MainActivity
 * so we can show a loading indicator and run it on a coroutine.
 */
class KanaReadApplication : Application() {

    val database: KanaDatabase by lazy { KanaDatabase.getInstance(this) }
}
