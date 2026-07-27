package sk.punkacidetom.pd2026

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sk.punkacidetom.pd2026.core.data.repository.BandRepositoryImpl
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.i18n.LocaleHelper
import sk.punkacidetom.pd2026.core.model.BandNotificationScheduler
import sk.punkacidetom.pd2026.core.notifications.NotificationChannelInitializer
import sk.punkacidetom.pd2026.core.ui.components.PD2026Scaffold
import sk.punkacidetom.pd2026.core.ui.theme.PD2026Theme
import sk.punkacidetom.pd2026.navigation.AppBottomBar
import sk.punkacidetom.pd2026.navigation.AppNavHost
import sk.punkacidetom.pd2026.navigation.HomeRoute
import sk.punkacidetom.pd2026.navigation.TimetableRoute
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var bandRepository: BandRepositoryImpl
    @Inject lateinit var userPrefs: UserPreferencesRepository
    @Inject lateinit var localeHelper: LocaleHelper
    @Inject lateinit var bandNotificationScheduler: BandNotificationScheduler

    /** Tracks exact-alarm permission state across resume cycles (in-memory only). */
    private var hadExactAlarmPermission = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — app works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Seed language from device locale on first launch (no-op if already set)
        // 2. Apply the persisted language so resources load in the correct locale
        lifecycleScope.launch {
            userPrefs.initLanguageIfAbsent(java.util.Locale.getDefault().language)
            val lang = userPrefs.language.first()
            localeHelper.applyLocale(lang)
        }

        NotificationChannelInitializer.createChannels(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            val isFontLarge by userPrefs.isFontLarge.collectAsState(initial = false)
            val fontScale = if (isFontLarge) 1.60f else 1.12f

            // Collect bands for NowPlayingHeader + start-destination decision
            val bands by bandRepository.observeBands().collectAsState(initial = emptyList())
            val festivalInfo by bandRepository.observeFestivalInfo().collectAsState(initial = null)

            val startDestination = if (festivalInfo?.phase(java.time.LocalDateTime.now()) ==
                sk.punkacidetom.pd2026.core.model.FestivalInfo.Phase.DURING
            ) TimetableRoute else HomeRoute

            // One-time exact-alarm permission rationale dialog
            var showExactAlarmDialog by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(AlarmManager::class.java)
                    if (!alarmManager.canScheduleExactAlarms() &&
                        !userPrefs.exactAlarmDialogShown.first()
                    ) {
                        userPrefs.setExactAlarmDialogShown()
                        showExactAlarmDialog = true
                    }
                }
            }

            PD2026Theme(fontScaleMultiplier = fontScale) {
                if (showExactAlarmDialog) {
                    AlertDialog(
                        onDismissRequest = { showExactAlarmDialog = false },
                        title   = { Text(stringResource(R.string.perm_alarm_dialog_title)) },
                        text    = { Text(stringResource(R.string.perm_alarm_dialog_body)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showExactAlarmDialog = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    startActivity(
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }
                            }) {
                                Text(stringResource(R.string.perm_alarm_dialog_confirm))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExactAlarmDialog = false }) {
                                Text(stringResource(R.string.perm_alarm_dialog_dismiss))
                            }
                        },
                    )
                }

                val navController = rememberNavController()
                PD2026Scaffold(
                    bands = bands,
                    bottomBar = { AppBottomBar(navController) },
                    onNowPlayingBandClick = { bandId ->
                        navController.navigate(
                            sk.punkacidetom.pd2026.navigation.BandDetailRoute(bandId)
                        )
                    },
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // SWR: refresh data on resume if >30 min since last fetch
        lifecycleScope.launch {
            bandRepository.refreshIfStale()
        }
        // Reschedule all favourite-band alarms when exact-alarm permission is just granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            val hasPermissionNow = alarmManager.canScheduleExactAlarms()
            if (!hadExactAlarmPermission && hasPermissionNow) {
                lifecycleScope.launch {
                    if (userPrefs.notificationsEnabled.first()) {
                        bandNotificationScheduler.rescheduleAll()
                    }
                }
            }
            hadExactAlarmPermission = hasPermissionNow
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
