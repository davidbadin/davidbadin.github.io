package sk.punkacidetom.pd2026

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PD2026App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Subscribe to the single broadcast topic (all devices, Slovak-only notifications)
        FirebaseMessaging.getInstance().subscribeToTopic("pd2026_all")
    }
}
