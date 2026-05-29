package sk.punkacidetom.pd2026.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import sk.punkacidetom.pd2026.feature.bands.navigation.bandsNavGraph
import sk.punkacidetom.pd2026.feature.home.navigation.homeNavGraph
import sk.punkacidetom.pd2026.feature.info.navigation.infoNavGraph
import sk.punkacidetom.pd2026.feature.news.navigation.newsNavGraph
import sk.punkacidetom.pd2026.feature.settings.navigation.settingsNavGraph
import sk.punkacidetom.pd2026.feature.spotify.navigation.spotifyNavGraph
import sk.punkacidetom.pd2026.feature.tickets.navigation.ticketsNavGraph
import sk.punkacidetom.pd2026.feature.timetable.navigation.timetableNavGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // Each feature module contributes its own sub-graph.
        // Remove a feature: delete the include() in settings.gradle.kts + the line below.
        homeNavGraph(navController)
        timetableNavGraph(navController)
        bandsNavGraph(navController)
        newsNavGraph(navController)
        infoNavGraph(navController)
        ticketsNavGraph(navController)
        settingsNavGraph(navController)
        spotifyNavGraph(navController)
    }
}
