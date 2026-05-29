package sk.punkacidetom.pd2026

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.i18n.LocaleHelper

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserPrefsEntryPoint {
    fun userPrefsRepository(): UserPreferencesRepository
}

@HiltAndroidApp
class PD2026App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Apply the saved locale before any Activity is created so the OS uses
        // the correct resource qualifiers from the very first frame.
        // runBlocking is acceptable here: DataStore reads a tiny cached preference.
        val userPrefs = EntryPoints.get(this, UserPrefsEntryPoint::class.java)
            .userPrefsRepository()
        val savedLang = runBlocking { userPrefs.language.first() }  // defaults to "sk"
        LocaleHelper().applyLocale(savedLang)

        // Subscribe to the single broadcast topic (all devices, Slovak-only notifications)
        FirebaseMessaging.getInstance().subscribeToTopic("pd2026_all")
    }
}
