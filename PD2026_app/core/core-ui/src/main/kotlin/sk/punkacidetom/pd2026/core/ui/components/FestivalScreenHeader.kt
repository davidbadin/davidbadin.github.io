package sk.punkacidetom.pd2026.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.White

/**
 * Static (non-scrolling) header used by the Schedule screen.
 * Renders header_main.png background, logo_pd_short.png at 90% width, and a screen title
 * using a simple padding-based layout.
 *
 * For Bands / News / Tickets / Settings use the parallax pattern directly in each screen.
 */
@Composable
fun FestivalScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background image — full width, natural height
            AsyncImage(
                model = "file:///android_asset/header_main.png",
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )

            // Logo and title overlaid on background image
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(SHORT_HEADER_LOGO_TOP_PADDING_DP.dp))
                AsyncImage(
                    model = "file:///android_asset/logo_pd_short.png",
                    contentDescription = "Punkáči deťom 2026",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(SHORT_HEADER_LOGO_WIDTH_FRACTION),
                )
                Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_TOP_PADDING_DP.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium,
                    color = White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md),
                )
            }
        }
    }
}
