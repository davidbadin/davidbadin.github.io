package sk.punkacidetom.pd2026.feature.tickets

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.NAV_BUTTON_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_BOTTOM_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.White

@Composable
fun TicketsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val spacing = LocalAppSpacing.current
    val scrollState = rememberScrollState()

    fun openUrl(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }

    Box(modifier = modifier.fillMaxSize().background(Navy)) {

        // Single scrollable column — background image scrolls at 1× with content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header area: Box sized by logo/title Column; background clipped to that height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clipToBounds(),
            ) {
                // Background image — fills Box (sized by content Column below); clipped at bottom
                AsyncImage(
                    model = "file:///android_asset/header_main.png",
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.matchParentSize(),
                )

                // Logo + title — sizes the Box; pushed below status bar
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
                        text = stringResource(R.string.tickets_title),
                        style = MaterialTheme.typography.displayMedium,
                        color = White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md),
                    )
                    Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_BOTTOM_PADDING_DP.dp))
                }
            }

            // Screen content — outer Box provides Navy background colour for uncovered area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TicketButton(label = stringResource(R.string.tickets_buy)) {
                    openUrl("https://punkacidetom.sk/vstupenky/")
                }
                Spacer(modifier = Modifier.height(spacing.md))
                TicketButton(label = stringResource(R.string.tickets_goout)) {
                    openUrl("https://goout.net/sk/punkaci-detom-2026/szbuqay/")
                }
                Spacer(modifier = Modifier.height(spacing.md))
                TicketButton(label = stringResource(R.string.tickets_eshop)) {
                    openUrl("https://shop.punkacidetom.sk/")
                }
            }
        }
    }
}

@Composable
private fun TicketButton(label: String, onClick: () -> Unit) {
    val spacing = LocalAppSpacing.current
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(NAV_BUTTON_WIDTH_FRACTION)
            .height(spacing.homeButtonMinHeight),
        colors = ButtonDefaults.buttonColors(containerColor = Crimson),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = White,
        )
    }
}
