package sk.punkacidetom.pd2026.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

private val PD2026ColorScheme = darkColorScheme(
    primary = Crimson,
    onPrimary = OnCrimson,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = White,
    secondary = CrimsonLight,
    onSecondary = OnCrimson,
    background = Navy,
    onBackground = White,
    surface = NavyLight,
    onSurface = White,
    surfaceVariant = NavyDark,
    onSurfaceVariant = WhiteAlpha60,
    outline = WhiteAlpha30,
    error = CrimsonLight,
    onError = White,
)

@Composable
fun PD2026Theme(
    fontScaleMultiplier: Float = 1.0f,
    content: @Composable () -> Unit,
) {
    val typography = remember(fontScaleMultiplier) { buildTypography(fontScaleMultiplier) }
    val spacing = remember(fontScaleMultiplier) { AppSpacing().scale(fontScaleMultiplier) }

    CompositionLocalProvider(
        LocalFontScaleMultiplier provides fontScaleMultiplier,
        LocalAppSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = PD2026ColorScheme,
            typography = typography,
            content = content,
        )
    }
}
