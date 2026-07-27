package sk.punkacidetom.pd2026.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.model.BandNotificationScheduler
import javax.inject.Inject

/**
 * Reschedules all favourite-band alarms after a device reboot.
 * Android clears all AlarmManager alarms on reboot; this receiver recreates them.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var bandNotificationScheduler: BandNotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                bandNotificationScheduler.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
