package sk.punkacidetom.pd2026.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.White

private const val SHORT_HEADER_LOGO_WIDTH_FRACTION   = 0.50f
private const val SHORT_HEADER_LOGO_CENTER_FRACTION  = 0.20f
private const val SHORT_HEADER_TITLE_CENTER_FRACTION = 0.50f

/**
 * Shared pinned header used by secondary screens (Timetable, Bands, News, Tickets, Settings).
 *
 * Renders a full-width background image (header_short.png), a logo overlay centred at 20%
 * of the background height, and a screen title centred at 50%.
 *
 * [onContentStartY] is called with the pixel Y offset at which screen content should begin
 * (bottom edge of the title text at 50% of the bg height). The caller uses this to size a
 * transparent spacer so the scrollable content starts at the right position.
 */
@Composable
fun FestivalScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onContentStartY: (Int) -> Unit = {},
) {
    val density       = LocalDensity.current
    val spacing       = LocalAppSpacing.current
    var bgHeightPx    by remember { mutableIntStateOf(0) }
    var logoHeightPx  by remember { mutableIntStateOf(0) }
    var titleHeightPx by remember { mutableIntStateOf(0) }
    val bgHeightDp    = with(density) { bgHeightPx.toDp() }
    val logoHeightDp  = with(density) { logoHeightPx.toDp() }
    val titleHeightDp = with(density) { titleHeightPx.toDp() }

    // Report content start Y (bottom edge of title) once measurements are available
    LaunchedEffect(bgHeightPx, titleHeightPx) {
        if (bgHeightPx > 0 && titleHeightPx > 0) {
            val centerY = (bgHeightPx * SHORT_HEADER_TITLE_CENTER_FRACTION).toInt()
            onContentStartY(centerY + titleHeightPx / 2)
        }
    }

    // No explicit height — Box height equals the full background image height (no clipping)
    Box(modifier = modifier.fillMaxWidth()) {

        // Background image — full width, natural height
        AsyncImage(
            model = "file:///android_asset/header_short.png",
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { bgHeightPx = it.height },
        )

        if (bgHeightPx > 0) {
            // Logo centred at 20% of background height
            AsyncImage(
                model = "file:///android_asset/logo_pd_short.png",
                contentDescription = "Punkáči deťom 2026",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth(SHORT_HEADER_LOGO_WIDTH_FRACTION)
                    .align(Alignment.TopCenter)
                    .onSizeChanged { logoHeightPx = it.height }
                    .offset(y = bgHeightDp * SHORT_HEADER_LOGO_CENTER_FRACTION - logoHeightDp / 2),
            )

            // Title centred at 50% of background height, left-aligned with horizontal padding
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .onSizeChanged { titleHeightPx = it.height }
                    .offset(y = bgHeightDp * SHORT_HEADER_TITLE_CENTER_FRACTION - titleHeightDp / 2)
                    .padding(horizontal = spacing.md),
            )
        }
    }
}
