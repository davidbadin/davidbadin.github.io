package sk.punkacidetom.pd2026.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.home.HomeScreen
import sk.punkacidetom.pd2026.navigation.BandsRoute
import sk.punkacidetom.pd2026.navigation.HomeRoute
import sk.punkacidetom.pd2026.navigation.InfoRoute
import sk.punkacidetom.pd2026.navigation.NewsRoute
import sk.punkacidetom.pd2026.navigation.SpotifyRoute
import sk.punkacidetom.pd2026.navigation.TicketsRoute

fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
    composable<HomeRoute> {
        HomeScreen(
            onNavigateToNews = { navController.navigate(NewsRoute) },
            onNavigateToBands = { navController.navigate(BandsRoute) },
            onNavigateToInfo = { navController.navigate(InfoRoute) },
            onNavigateToTickets = { navController.navigate(TicketsRoute) },
            onNavigateToSpotify = {
                navController.navigate(SpotifyRoute) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}
