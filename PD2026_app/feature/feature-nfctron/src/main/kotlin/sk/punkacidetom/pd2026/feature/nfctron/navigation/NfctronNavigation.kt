package sk.punkacidetom.pd2026.feature.nfctron.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.nfctron.NfctronScreen

fun NavGraphBuilder.nfctronNavGraph(navController: NavHostController) {
    composable<sk.punkacidetom.pd2026.navigation.NfctronRoute> {
        NfctronScreen()
    }
}
