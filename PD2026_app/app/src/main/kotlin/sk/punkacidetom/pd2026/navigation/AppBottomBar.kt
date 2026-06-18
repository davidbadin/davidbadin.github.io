package sk.punkacidetom.pd2026.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import sk.punkacidetom.pd2026.R
import sk.punkacidetom.pd2026.core.ui.icons.FaFamily
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.NavyDark
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

@Composable
fun AppBottomBar(navController: NavHostController) {
    val spacing = LocalAppSpacing.current
    val currentBackStack = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack.value?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(spacing.bottomNavHeight)
            .background(NavyDark),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomNavItem(
            label = stringResource(R.string.nav_home),
            icon = { isSelected ->
                FaIcon(
                    name = "house",
                    size = spacing.iconLg,
                    tint = if (isSelected) Crimson else WhiteAlpha60,
                )
            },
            isSelected = currentRoute?.contains("HomeRoute") == true,
            onClick = {
                navController.navigate(HomeRoute) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        BottomNavItem(
            label = stringResource(R.string.nav_timetable),
            icon = { isSelected ->
                FaIcon(
                    name = "calendar",
                    size = spacing.iconLg,
                    tint = if (isSelected) Crimson else WhiteAlpha60,
                )
            },
            isSelected = currentRoute?.contains("TimetableRoute") == true,
            onClick = {
                navController.navigate(TimetableRoute) {
                    // Pop Timetable (inclusive) before re-navigating, so any BandDetail
                    // sitting on top of it is cleared and a fresh TimetableScreen appears.
                    popUpTo<TimetableRoute> { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        BottomNavItem(
            label = stringResource(R.string.nav_spotify),
            icon = { isSelected ->
                FaIcon(
                    name = "spotify",
                    family = FaFamily.Brands,
                    size = spacing.iconLg,
                    tint = if (isSelected) Crimson else WhiteAlpha60,
                )
            },
            isSelected = currentRoute?.contains("SpotifyRoute") == true,
            onClick = {
                navController.navigate(SpotifyRoute) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        BottomNavItem(
            label = stringResource(R.string.nav_settings),
            icon = { isSelected ->
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = if (isSelected) Crimson else WhiteAlpha60,
                    modifier = Modifier.size(spacing.iconLg),
                )
            },
            isSelected = currentRoute?.contains("SettingsRoute") == true,
            onClick = {
                navController.navigate(SettingsRoute) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: @Composable (Boolean) -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon(isSelected)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Crimson else WhiteAlpha60,
        )
    }
}
