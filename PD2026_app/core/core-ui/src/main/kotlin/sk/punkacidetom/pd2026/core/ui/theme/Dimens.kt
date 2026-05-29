package sk.punkacidetom.pd2026.core.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Accessed via LocalAppSpacing.current — scales with the font-scale multiplier
data class AppSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val cardCorner: Dp = 8.dp,
    val buttonMinHeight: Dp = 48.dp,
    val bottomNavHeight: Dp = 64.dp,
    val nowPlayingHeaderHeight: Dp = 56.dp,
    val bandImageHeight: Dp = 260.dp,
    val homeButtonMinHeight: Dp = 56.dp,
    val iconSm: Dp = 16.dp,
    val iconMd: Dp = 20.dp,
    val iconLg: Dp = 24.dp,
)

val LocalAppSpacing = compositionLocalOf { AppSpacing() }

// Scales an AppSpacing by a multiplier (Normal=1.0, Large=1.3)
fun AppSpacing.scale(factor: Float) = AppSpacing(
    xs = (xs.value * factor).dp,
    sm = (sm.value * factor).dp,
    md = (md.value * factor).dp,
    lg = (lg.value * factor).dp,
    xl = (xl.value * factor).dp,
    xxl = (xxl.value * factor).dp,
    cardCorner = cardCorner,
    buttonMinHeight = (buttonMinHeight.value * factor).dp,
    bottomNavHeight = (bottomNavHeight.value * factor).dp,
    nowPlayingHeaderHeight = (nowPlayingHeaderHeight.value * factor).dp,
    bandImageHeight = bandImageHeight,
    homeButtonMinHeight = (homeButtonMinHeight.value * factor).dp,
    iconSm = (iconSm.value * factor).dp,
    iconMd = (iconMd.value * factor).dp,
    iconLg = (iconLg.value * factor).dp,
)

val LocalFontScaleMultiplier = compositionLocalOf { 1.0f }
