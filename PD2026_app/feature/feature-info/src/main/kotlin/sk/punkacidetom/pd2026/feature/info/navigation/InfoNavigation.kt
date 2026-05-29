package sk.punkacidetom.pd2026.feature.info.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.info.InfoScreen

fun NavGraphBuilder.infoNavGraph(navController: NavHostController) {
    composable<sk.punkacidetom.pd2026.navigation.InfoRoute> {
        InfoScreen()
    }
}
