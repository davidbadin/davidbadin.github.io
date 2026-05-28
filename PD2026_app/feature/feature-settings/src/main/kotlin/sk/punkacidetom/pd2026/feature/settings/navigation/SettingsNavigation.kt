package sk.punkacidetom.pd2026.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsNavGraph(navController: NavHostController) {
    composable<sk.punkacidetom.pd2026.navigation.SettingsRoute> {
        SettingsScreen()
    }
}
