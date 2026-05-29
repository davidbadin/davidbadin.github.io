package sk.punkacidetom.pd2026.feature.news.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import sk.punkacidetom.pd2026.feature.news.NewsScreen

fun NavGraphBuilder.newsNavGraph(navController: NavHostController) {
    composable<sk.punkacidetom.pd2026.navigation.NewsRoute> {
        NewsScreen()
    }
}
