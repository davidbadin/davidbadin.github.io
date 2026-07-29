package sk.punkacidetom.pd2026.core.model

/**
 * Schedules and cancels local AlarmManager-based reminders for favourite bands.
 * Interface lives in core-model (no Android deps) so both core-data and
 * core-notifications can reference it without a circular dependency.
 */
interface BandNotificationScheduler {
    /** Schedule a reminder 5 min before [band] starts. No-op if start time is past. */
    suspend fun scheduleNotification(band: Band)
    /** Cancel the reminder for [bandId] and remove it from the DataStore map. */
    suspend fun cancelNotification(bandId: Int)
    /** Cancel ALL scheduled band reminders. */
    suspend fun cancelAllNotifications()
    /** Re-schedule all reminders stored in DataStore (called after reboot or language change). */
    suspend fun rescheduleAll()
    /**
     * Called after every successful CSV fetch. Cancels alarms for bands no longer in the
     * dataset and reschedules bands whose start time changed.
     */
    suspend fun syncWithUpdatedBands(updatedBands: List<Band>)
}
