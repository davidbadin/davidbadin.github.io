package sk.punkacidetom.pd2026.feature.spotify.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.spotify.SpotifyScreen

fun NavGraphBuilder.spotifyNavGraph(navController: NavHostController) {
    composable<sk.punkacidetom.pd2026.navigation.SpotifyRoute> {
        SpotifyScreen()
    }
}
