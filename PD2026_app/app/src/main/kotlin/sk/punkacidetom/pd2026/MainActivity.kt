package sk.punkacidetom.pd2026

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import sk.punkacidetom.pd2026.core.data.repository.BandRepositoryImpl
import sk.punkacidetom.pd2026.core.data.repository.UserPreferencesRepository
import sk.punkacidetom.pd2026.core.ui.components.PD2026Scaffold
import sk.punkacidetom.pd2026.core.ui.theme.PD2026Theme
import sk.punkacidetom.pd2026.navigation.AppBottomBar
import sk.punkacidetom.pd2026.navigation.AppNavHost
import sk.punkacidetom.pd2026.navigation.HomeRoute
import sk.punkacidetom.pd2026.navigation.TimetableRoute
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var bandRepository: BandRepositoryImpl
    @Inject lateinit var userPrefs: UserPreferencesRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — app works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            val isFontLarge by userPrefs.isFontLarge.collectAsState(initial = false)
            val fontScale = if (isFontLarge) 1.30f else 1.0f

            // Collect bands for NowPlayingHeader + start-destination decision
            val bands by bandRepository.observeBands().collectAsState(initial = emptyList())
            val festivalInfo by bandRepository.observeFestivalInfo().collectAsState(initial = null)

            val startDestination = if (festivalInfo?.phase(java.time.LocalDateTime.now()) ==
                sk.punkacidetom.pd2026.core.model.FestivalInfo.Phase.DURING
            ) TimetableRoute else HomeRoute

            PD2026Theme(fontScaleMultiplier = fontScale) {
                val navController = rememberNavController()
                PD2026Scaffold(
                    bands = bands,
                    bottomBar = { AppBottomBar(navController) },
                    onNowPlayingBandClick = { bandId ->
                        navController.navigate(
                            sk.punkacidetom.pd2026.navigation.BandDetailRoute(bandId)
                        )
                    },
                ) { _ ->
                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize(),
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
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
