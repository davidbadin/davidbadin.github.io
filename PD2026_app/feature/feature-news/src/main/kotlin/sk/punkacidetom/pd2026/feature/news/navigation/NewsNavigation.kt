package sk.punkacidetom.pd2026.feature.news.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import sk.punkacidetom.pd2026.feature.news.NewsScreen
import sk.punkacidetom.pd2026.feature.news.NewsletterVolumeScreen
import sk.punkacidetom.pd2026.navigation.NewsletterVolumeRoute
import sk.punkacidetom.pd2026.navigation.NewsRoute

fun NavGraphBuilder.newsNavGraph(navController: NavHostController) {
    composable<NewsRoute> {
        NewsScreen(onOpenVolume = { volumeId ->
            navController.navigate(NewsletterVolumeRoute(volumeId))
        })
    }
    composable<NewsletterVolumeRoute> {
        NewsletterVolumeScreen()
    }
}
