package sk.punkacidetom.pd2026.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelInitializer {
    /**
     * Creates all notification channels for the app.
     * Safe to call multiple times — Android deduplicates existing channels.
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bandRemindersChannel = NotificationChannel(
                NotificationConstants.BAND_REMINDER_CHANNEL_ID,
                NotificationConstants.BAND_REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Reminds you before your favourite bands start"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(bandRemindersChannel)
        }
    }
}
