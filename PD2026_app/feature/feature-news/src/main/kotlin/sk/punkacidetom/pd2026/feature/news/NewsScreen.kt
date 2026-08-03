package sk.punkacidetom.pd2026.feature.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.NAV_BUTTON_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

@Composable
fun NewsScreen(
    onOpenVolume: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalAppSpacing.current
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize().background(Navy)) {

        // Single scrollable column — background image scrolls at 1× with content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header area: background image + logo + title in one Box
            Box(modifier = Modifier.fillMaxWidth()) {

                // Background image — layer 1, scrolls at 1×; extends behind status bar
                AsyncImage(
                    model = "file:///android_asset/header_main.png",
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Logo + title — layer 2, pushed below status bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
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
                        text = stringResource(R.string.newsletter_title),
                        style = MaterialTheme.typography.displayMedium,
                        color = White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md),
                    )
                }
            }

            // Screen content — outer Box provides Navy background colour for uncovered area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (uiState.volumes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.newsletter_not_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteAlpha60,
                    )
                } else {
                    uiState.volumes.forEach { volume ->
                        Button(
                            onClick = { onOpenVolume(volume.id) },
                            modifier = Modifier
                                .fillMaxWidth(NAV_BUTTON_WIDTH_FRACTION)
                                .height(spacing.homeButtonMinHeight),
                            colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                        ) {
                            Text(
                                text = stringResource(R.string.newsletter_volume, volume.id),
                                style = MaterialTheme.typography.headlineMedium,
                                color = White,
                            )
                        }
                        Spacer(modifier = Modifier.height(spacing.sm))
                    }
                }
            }
        }
    }
}
