package sk.punkacidetom.pd2026.feature.bands.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.bands.BandDetailScreen
import sk.punkacidetom.pd2026.feature.bands.BandsScreen
import sk.punkacidetom.pd2026.navigation.BandDetailRoute
import sk.punkacidetom.pd2026.navigation.BandsRoute

fun NavGraphBuilder.bandsNavGraph(navController: NavHostController) {
    composable<BandsRoute> {
        BandsScreen(
            onBandClick = { bandId -> navController.navigate(BandDetailRoute(bandId)) },
        )
    }
    composable<BandDetailRoute> {
        BandDetailScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
