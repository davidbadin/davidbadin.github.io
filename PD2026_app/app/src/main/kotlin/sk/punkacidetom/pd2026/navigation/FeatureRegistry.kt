package sk.punkacidetom.pd2026.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * Central list of feature nav-graph contributions and bottom-bar entries.
 *
 * To REMOVE a feature:
 *   1. Remove its `include(":feature:feature-xxx")` line in settings.gradle.kts
 *   2. Remove its entry below (the compiler will catch any remaining references)
 *
 * To ADD a feature:
 *   1. Add the module to settings.gradle.kts
 *   2. Add a BottomNavEntry here if it needs a bottom-bar tab
 *   3. Call its navGraph builder in AppNavHost.kt
 */
object FeatureRegistry {

    data class BottomNavEntry(
        val route: Any,
        val labelRes: Int,
        val icon: @Composable () -> Unit,
    )
}
