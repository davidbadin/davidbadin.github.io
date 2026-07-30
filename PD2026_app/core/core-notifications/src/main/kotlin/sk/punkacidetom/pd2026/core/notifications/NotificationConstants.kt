package sk.punkacidetom.pd2026.core.notifications

object NotificationConstants {
    const val BAND_REMINDER_MINUTES_BEFORE = 10
    const val BAND_REMINDER_CHANNEL_ID     = "band_reminders"
    const val BAND_REMINDER_CHANNEL_NAME   = "Band reminders"

    // Intent extras used by BandAlarmReceiver — pre-resolved at schedule time
    const val EXTRA_BAND_ID     = "band_id"
    const val EXTRA_NOTIF_TITLE = "notif_title"
    const val EXTRA_NOTIF_TEXT  = "notif_text"
}
