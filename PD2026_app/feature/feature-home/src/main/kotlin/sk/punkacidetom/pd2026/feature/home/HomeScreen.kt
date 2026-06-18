package sk.punkacidetom.pd2026.feature.home

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import sk.punkacidetom.pd2026.core.model.FestivalInfo
import sk.punkacidetom.pd2026.core.ui.icons.FaFamily
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

private const val URL_FACEBOOK = "https://www.facebook.com/punkacidetom"
private const val URL_INSTAGRAM = "https://www.instagram.com/festival_punkaci_detom/"

@Composable
fun HomeScreen(
    onNavigateToNews: () -> Unit,
    onNavigateToBands: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onNavigateToTickets: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isNewsletterAvailable by viewModel.isNewsletterAvailable.collectAsState()
    val spacing = LocalAppSpacing.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(spacing.lg))

        // Festival logo
        Image(
            painter = painterResource(R.drawable.logo_pd),
            contentDescription = "Punkáči deťom 2026",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xl),
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        // Phase-specific block
        when (uiState.phase) {
            FestivalInfo.Phase.BEFORE -> CountdownBlock(uiState.countdown, spacing.md)
            FestivalInfo.Phase.AFTER -> ThankyouBlock(uiState.thankyouText)
            FestivalInfo.Phase.DURING -> {}
        }

        Spacer(modifier = Modifier.height(spacing.lg))

        // Navigation buttons — single column, full width
        val navButtons = buildList {
            if (isNewsletterAvailable) {
                add(Triple(stringResource(R.string.home_btn_newsletter), "newspaper", onNavigateToNews))
            }
            add(Triple(stringResource(R.string.home_btn_timetable), "calendar", onNavigateToTimetable))
            add(Triple(stringResource(R.string.home_btn_bands), "music", onNavigateToBands))
            add(Triple(stringResource(R.string.home_btn_info), "circle-info", onNavigateToInfo))
            add(Triple(stringResource(R.string.home_btn_tickets), "ticket", onNavigateToTickets))
        }

        navButtons.forEach { (label, icon, onClick) ->
            HomeNavButton(
                label = label,
                icon = icon,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(spacing.sm))
        }

        Spacer(modifier = Modifier.height(spacing.lg))

        // Social links — stacked full width
        Text(
            text = stringResource(R.string.home_social_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = White,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        SocialButton(
            label = stringResource(R.string.home_social_facebook),
            icon = "facebook",
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(URL_FACEBOOK))
            },
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        SocialButton(
            label = stringResource(R.string.home_social_instagram),
            icon = "instagram",
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(URL_INSTAGRAM))
            },
        )

        Spacer(modifier = Modifier.height(spacing.xl))
    }
}

@Composable
private fun CountdownBlock(countdown: CountdownState, paddingMd: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyLight, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(paddingMd),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_countdown_until),
            style = MaterialTheme.typography.titleMedium,
            color = WhiteAlpha60,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CountdownUnit(countdown.days, stringResource(R.string.home_countdown_days))
            CountdownUnit(countdown.hours, stringResource(R.string.home_countdown_hours))
            CountdownUnit(countdown.minutes, stringResource(R.string.home_countdown_minutes))
            CountdownUnit(countdown.seconds, stringResource(R.string.home_countdown_seconds))
        }
    }
}

@Composable
private fun CountdownUnit(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displayMedium,
            color = Crimson,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WhiteAlpha60,
        )
    }
}

@Composable
private fun ThankyouBlock(text: String) {
    if (text.isNotBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyLight, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(16.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HomeNavButton(
    label: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Button(
        onClick = onClick,
        modifier = modifier.height(spacing.homeButtonMinHeight),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = White),
    ) {
        FaIcon(name = icon, size = spacing.iconMd, tint = Crimson, modifier = Modifier.padding(end = 6.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Navy)
    }
}

@Composable
private fun SocialButton(
    label: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Button(
        onClick = onClick,
        modifier = modifier.height(spacing.buttonMinHeight),
        colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
    ) {
        FaIcon(
            name = icon,
            family = FaFamily.Brands,
            size = spacing.iconMd,
            tint = Crimson,   // was White
            modifier = Modifier.padding(end = 6.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
