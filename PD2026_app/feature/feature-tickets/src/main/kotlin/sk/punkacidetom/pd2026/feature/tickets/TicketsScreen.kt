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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import sk.punkacidetom.pd2026.core.ui.components.FestivalScreenHeader
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.NAV_BUTTON_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White

@Composable
fun TicketsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val spacing = LocalAppSpacing.current
    val density = LocalDensity.current

    fun openUrl(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }

    var contentStartPx by remember { mutableIntStateOf(0) }
    val contentStartDp = with(density) { contentStartPx.toDp() }

    Box(modifier = modifier.fillMaxSize().background(Navy)) {

        // Layer 1: static pinned header
        FestivalScreenHeader(
            title = stringResource(R.string.tickets_title),
            onContentStartY = { contentStartPx = it },
        )

        // Layer 2: scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Transparent spacer — header visible behind it
            Spacer(modifier = Modifier.height(contentStartDp))

            // Navy-backed content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy)
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
