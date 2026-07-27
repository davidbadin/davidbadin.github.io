package sk.punkacidetom.pd2026.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import sk.punkacidetom.pd2026.core.data.repository.BandRepository
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.BandNotificationScheduler
import sk.punkacidetom.pd2026.core.model.Stages
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BandNotificationSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bandRepository: BandRepository,
    private val userPrefs: UserPreferencesRepository,
) : BandNotificationScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    // ------------------------------------------------------------------
    // scheduleNotification
    // ------------------------------------------------------------------

    override suspend fun scheduleNotification(band: Band) {
        val triggerAtMs = LocalDateTime.of(band.startDate, band.startTime)
            .minusMinutes(NotificationConstants.BAND_REMINDER_MINUTES_BEFORE.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Guard: reminder time already in the past
        if (triggerAtMs <= System.currentTimeMillis()) return

        // Guard: exact-alarm permission (Android 12+ / API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                userPrefs.setExactAlarmPermissionMissing(true)
                return
            }
        }
        userPrefs.setExactAlarmPermissionMissing(false)

        // Resolve localised notification text at schedule time — the receiver
        // has no access to DataStore so strings are baked into the PendingIntent.
        val lang      = userPrefs.language.first()
        val stageName = Stages.displayName(band.stageCode)
        val title     = band.name
        val text = if (lang == "sk")
            "${band.name} už o chvíľu na $stageName! Vidíme sa pred pódiom!"
        else
            "${band.name} soon on the $stageName! See you in front of the stage!"

        val alarmIntent = Intent(context, BandAlarmReceiver::class.java).apply {
            putExtra(NotificationConstants.EXTRA_BAND_ID,     band.id)
            putExtra(NotificationConstants.EXTRA_NOTIF_TITLE, title)
            putExtra(NotificationConstants.EXTRA_NOTIF_TEXT,  text)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            band.id,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMs,
            pendingIntent,
        )

        userPrefs.saveScheduledNotification(band.id, triggerAtMs)
    }

    // ------------------------------------------------------------------
    // cancelNotification
    // ------------------------------------------------------------------

    override suspend fun cancelNotification(bandId: Int) {
        val alarmIntent = Intent(context, BandAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            bandId,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        userPrefs.removeScheduledNotification(bandId)
    }

    // ------------------------------------------------------------------
    // cancelAllNotifications
    // ------------------------------------------------------------------

    override suspend fun cancelAllNotifications() {
        val scheduled = userPrefs.getScheduledNotifications()
        scheduled.keys.forEach { cancelNotification(it) }
    }

    // ------------------------------------------------------------------
    // rescheduleAll  — called after reboot or language change
    // ------------------------------------------------------------------

    override suspend fun rescheduleAll() {
        val scheduled = userPrefs.getScheduledNotifications()
        if (scheduled.isEmpty()) return
        val allBands = bandRepository.observeBands().first()
        scheduled.keys.forEach { bandId ->
            val band = allBands.find { it.id == bandId } ?: return@forEach
            scheduleNotification(band)
        }
    }

    // ------------------------------------------------------------------
    // syncWithUpdatedBands  — called after a successful CSV fetch
    // ------------------------------------------------------------------

    override suspend fun syncWithUpdatedBands(updatedBands: List<Band>) {
        val scheduled = userPrefs.getScheduledNotifications()
        if (scheduled.isEmpty()) return

        val fetchedIds    = updatedBands.map { it.id }.toSet()
        val favouriteIds  = userPrefs.favouriteIds.first()

        // 1. Cancel alarms for bands that were removed from the data entirely
        scheduled.keys
            .filter { it !in fetchedIds }
            .forEach { cancelNotification(it) }

        // 2. Reschedule bands whose trigger time changed (or that are in favourites
        //    but whose alarm entry is missing — defensive catch-up)
        updatedBands
            .filter { it.id in favouriteIds }
            .forEach { band ->
                val newTriggerMs = LocalDateTime.of(band.startDate, band.startTime)
                    .minusMinutes(NotificationConstants.BAND_REMINDER_MINUTES_BEFORE.toLong())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val oldTriggerMs = scheduled[band.id]
                if (oldTriggerMs == null || oldTriggerMs != newTriggerMs) {
                    if (oldTriggerMs != null) cancelNotification(band.id)
                    scheduleNotification(band)
                }
            }
    }
}
