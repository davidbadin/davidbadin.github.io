package sk.punkacidetom.pd2026.feature.timetable.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.timetable.TimetableScreen
import sk.punkacidetom.pd2026.navigation.BandDetailRoute
import sk.punkacidetom.pd2026.navigation.TimetableRoute

fun NavGraphBuilder.timetableNavGraph(navController: NavHostController) {
    composable<TimetableRoute> {
        TimetableScreen(
            onBandClick = { bandId -> navController.navigate(BandDetailRoute(bandId)) },
        )
    }
}
