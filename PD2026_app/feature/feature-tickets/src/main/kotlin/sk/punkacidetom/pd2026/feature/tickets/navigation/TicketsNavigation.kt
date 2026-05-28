package sk.punkacidetom.pd2026.feature.tickets.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.tickets.TicketsScreen

fun NavGraphBuilder.ticketsNavGraph(navController: NavHostController) {
    composable<sk.punkacidetom.pd2026.navigation.TicketsRoute> {
        TicketsScreen()
    }
}
