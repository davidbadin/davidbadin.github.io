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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.NotificationGreen
import sk.punkacidetom.pd2026.core.ui.theme.White
import androidx.hilt.navigation.compose.hiltViewModel
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
            val viewModel: MainActivityViewModel = hiltViewModel()

            val isFontLarge by userPrefs.isFontLarge.collectAsState(initial = false)
            val fontScale = if (isFontLarge) 1.60f else 1.12f

            // Collect bands for NowPlayingHeader + start-destination decision
            val bands by bandRepository.observeBands().collectAsState(initial = emptyList())
            val festivalInfo by bandRepository.observeFestivalInfo().collectAsState(initial = null)

            val startDestination = if (festivalInfo?.phase(java.time.LocalDateTime.now()) ==
                sk.punkacidetom.pd2026.core.model.FestivalInfo.Phase.DURING
            ) TimetableRoute else HomeRoute

            val showDialog by viewModel.showExactAlarmDialog.collectAsState()

            PD2026Theme(fontScaleMultiplier = fontScale) {
                Box(modifier = Modifier.fillMaxSize()) {
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

                    if (showDialog) {
                        ExactAlarmRationaleDialog(
                            onConfirm = {
                                viewModel.onExactAlarmDialogConfirmed()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    startActivity(
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }
                            },
                            onDismiss = { viewModel.onExactAlarmDialogDismissed() },
                        )
                    }
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

        val hasPermissionNow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            getSystemService(AlarmManager::class.java).canScheduleExactAlarms() else true

        // Reschedule all favourite-band alarms when exact-alarm permission is just granted
        if (!hadExactAlarmPermission && hasPermissionNow) {
            lifecycleScope.launch {
                if (userPrefs.notificationsEnabled.first()) {
                    bandNotificationScheduler.rescheduleAll()
                }
            }
        }
        hadExactAlarmPermission = hasPermissionNow
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun ExactAlarmRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyLight,
        title   = { Text(stringResource(R.string.perm_alarm_dialog_title), color = White) },
        text    = { Text(stringResource(R.string.perm_alarm_dialog_body),  color = White) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NotificationGreen,
                    contentColor   = White,
                ),
            ) {
                Text(stringResource(R.string.perm_alarm_dialog_confirm))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Crimson,
                    contentColor   = White,
                ),
            ) {
                Text(stringResource(R.string.perm_alarm_dialog_dismiss))
            }
        },
    )
}
