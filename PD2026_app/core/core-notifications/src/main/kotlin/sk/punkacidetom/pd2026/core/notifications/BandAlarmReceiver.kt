package sk.punkacidetom.pd2026.core.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Fires when AlarmManager triggers a band-start reminder.
 * All strings (title, body) are pre-resolved at schedule time and passed as extras,
 * so this receiver needs no DataStore or resource access at fire time.
 */
class BandAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bandId = intent.getIntExtra(NotificationConstants.EXTRA_BAND_ID, -1)
        if (bandId == -1) return
        val title = intent.getStringExtra(NotificationConstants.EXTRA_NOTIF_TITLE) ?: return
        val text  = intent.getStringExtra(NotificationConstants.EXTRA_NOTIF_TEXT)  ?: return

        // Open the app when the notification is tapped
        val openAppIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            ?: return

        val pendingIntent = PendingIntent.getActivity(
            context, bandId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationConstants.BAND_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(bandId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission denied — skip silently, no crash
        }
    }
}
