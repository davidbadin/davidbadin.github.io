package sk.punkacidetom.pd2026.feature.tickets

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White

@Composable
fun TicketsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val spacing = LocalAppSpacing.current

    fun openUrl(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
    ) {
        Text(
            text = stringResource(R.string.tickets_title),
            style = MaterialTheme.typography.displayMedium,
            color = White,
        )
        Spacer(modifier = Modifier.height(spacing.lg))

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

@Composable
private fun TicketButton(label: String, onClick: () -> Unit) {
    val spacing = LocalAppSpacing.current
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
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
