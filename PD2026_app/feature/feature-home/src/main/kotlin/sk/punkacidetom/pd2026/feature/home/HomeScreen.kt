package sk.punkacidetom.pd2026.feature.home

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.model.FestivalInfo
import sk.punkacidetom.pd2026.core.ui.icons.FaFamily
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.NAV_BUTTON_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.PARALLAX_SCROLL_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.White

private const val URL_FACEBOOK  = "https://www.facebook.com/punkacidetom"
private const val URL_INSTAGRAM = "https://www.instagram.com/festival_punkaci_detom/"
private const val URL_WEBSITE   = "https://punkacidetom.sk/"

private const val MAIN_LOGO_WIDTH_FRACTION      = 0.90f
private const val MAIN_LOGO_TOP_PADDING_DP      = 60
private const val MAIN_CT_BLOCK_WIDTH_FRACTION  = 0.90f
private const val MAIN_CT_BLOCK_TOP_PADDING_DP  = 60
private const val MAIN_STRIPE_WIDTH_FRACTION    = 0.75f
private const val MAIN_COUNTDOWN_WIDTH_FRACTION = 0.90f
private const val MAIN_THANKYOU_WIDTH_FRACTION  = 0.75f
private const val MAIN_BUTTONS_TOP_PADDING_DP   = 48
private const val MAIN_SOCIAL_TOP_PADDING_DP    = 48

@Composable
fun HomeScreen(
    onNavigateToNews: () -> Unit,
    onNavigateToBands: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onNavigateToNfctron: () -> Unit,
    onNavigateToTickets: () -> Unit,
    onNavigateToSpotify: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isNewsletterAvailable by viewModel.isNewsletterAvailable.collectAsState()
    val spacing = LocalAppSpacing.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val navButtons = buildList {
        add(Triple(stringResource(R.string.home_btn_timetable), "calendar",    onNavigateToTimetable))
        add(Triple(stringResource(R.string.home_btn_bands),     "music",        onNavigateToBands))
        if (isNewsletterAvailable) {
            add(Triple(stringResource(R.string.home_btn_newsletter), "newspaper", onNavigateToNews))
        }
        add(Triple(stringResource(R.string.home_btn_info),    "circle-info", onNavigateToInfo))
        add(Triple(stringResource(R.string.home_btn_nfctron), "rss",         onNavigateToNfctron))
        add(Triple(stringResource(R.string.home_btn_tickets), "ticket",      onNavigateToTickets))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Navy),
    ) {
        // Layer 1: background image — clipped below status bar so it can't render behind it
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .clipToBounds(),
        ) {
            AsyncImage(
                model = "file:///android_asset/header_main.png",
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = -scrollState.value * PARALLAX_SCROLL_FRACTION },
            )
        }

        // Layer 2: all content — scrolls at 1×; statusBarsPadding keeps logo below status bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeHeader(
                phase        = uiState.phase,
                countdown    = uiState.countdown,
                thankyouText = uiState.thankyouText,
                navButtonsContent = {
                    navButtons.forEach { (label, icon, onClick) ->
                        HomeNavButton(
                            label    = label,
                            icon     = icon,
                            onClick  = onClick,
                            modifier = Modifier.fillMaxWidth(NAV_BUTTON_WIDTH_FRACTION),
                        )
                        Spacer(modifier = Modifier.height(spacing.sm))
                    }
                    HomeNavButton(
                        label      = stringResource(R.string.home_btn_spotify_playlist),
                        icon       = "spotify",
                        iconFamily = FaFamily.Brands,
                        onClick    = onNavigateToSpotify,
                        modifier   = Modifier.fillMaxWidth(NAV_BUTTON_WIDTH_FRACTION),
                    )
                    Spacer(modifier = Modifier.height(spacing.sm))
                },
                socialContent = {
                    Text(
                        text = stringResource(R.string.home_social_heading),
                        style = MaterialTheme.typography.headlineSmall,
                        color = White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SocialLink(
                            label = stringResource(R.string.home_social_facebook),
                            icon  = "facebook",
                            onClick = {
                                CustomTabsIntent.Builder().build()
                                    .launchUrl(context, Uri.parse(URL_FACEBOOK))
                            },
                        )
                        SocialLink(
                            label = stringResource(R.string.home_social_instagram),
                            icon  = "instagram",
                            onClick = {
                                CustomTabsIntent.Builder().build()
                                    .launchUrl(context, Uri.parse(URL_INSTAGRAM))
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.sm))
                    SocialLink(
                        label      = stringResource(R.string.home_social_website),
                        icon       = "globe",
                        iconFamily = FaFamily.Regular,
                        onClick    = {
                            CustomTabsIntent.Builder().build()
                                .launchUrl(context, Uri.parse(URL_WEBSITE))
                        },
                    )
                },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HomeHeader — padding-based logo + C/T block + navy content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    phase: FestivalInfo.Phase,
    countdown: CountdownState,
    thankyouText: String,
    navButtonsContent: @Composable ColumnScope.() -> Unit,
    socialContent: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalAppSpacing.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo
        Spacer(modifier = Modifier.height(MAIN_LOGO_TOP_PADDING_DP.dp))
        AsyncImage(
            model = "file:///android_asset/logo_pd_main.png",
            contentDescription = "Punkáči deťom 2026",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(MAIN_LOGO_WIDTH_FRACTION),
        )

        // Countdown / Thank-you block (hidden during festival)
        if (phase != FestivalInfo.Phase.DURING) {
            Spacer(modifier = Modifier.height(MAIN_CT_BLOCK_TOP_PADDING_DP.dp))
            PhaseBlock(
                phase        = phase,
                countdown    = countdown,
                thankyouText = thankyouText,
            )
        }

        // Navy-backed section — buttons + social
        Spacer(modifier = Modifier.height(MAIN_BUTTONS_TOP_PADDING_DP.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            navButtonsContent()

            Spacer(modifier = Modifier.height(MAIN_SOCIAL_TOP_PADDING_DP.dp))
            socialContent()

            Spacer(modifier = Modifier.height(spacing.xl))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PhaseBlock — stripe + countdown or thank-you (not shown during festival)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PhaseBlock(
    phase: FestivalInfo.Phase,
    countdown: CountdownState,
    thankyouText: String,
) {
    Box(
        modifier = Modifier.fillMaxWidth(MAIN_CT_BLOCK_WIDTH_FRACTION),
        contentAlignment = Alignment.Center,
    ) {
        // Stripe — 75% of screen = (0.75 / 0.90) of C/T block width
        AsyncImage(
            model = "file:///android_asset/stripe.png",
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(MAIN_STRIPE_WIDTH_FRACTION / MAIN_CT_BLOCK_WIDTH_FRACTION),
        )
        when (phase) {
            FestivalInfo.Phase.BEFORE -> CountdownContent(
                countdown = countdown,
                modifier  = Modifier.fillMaxWidth(),
            )
            FestivalInfo.Phase.AFTER  -> ThankyouContent(
                text     = thankyouText,
                modifier = Modifier.fillMaxWidth(MAIN_THANKYOU_WIDTH_FRACTION / MAIN_CT_BLOCK_WIDTH_FRACTION),
            )
            else -> {}
        }
    }
}

@Composable
private fun CountdownContent(
    countdown: CountdownState,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Column(
        modifier = modifier.padding(spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_countdown_until),
            style = MaterialTheme.typography.displayMedium,
            color = White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${countdown.days} ${stringResource(R.string.home_countdown_days)} " +
                   "${countdown.hours} ${stringResource(R.string.home_countdown_hours)} " +
                   "${countdown.minutes} ${stringResource(R.string.home_countdown_minutes)}",
            style = MaterialTheme.typography.displayMedium,
            color = White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ThankyouContent(
    text: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    if (text.isNotBlank()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = White,
            textAlign = TextAlign.Center,
            modifier = modifier.padding(spacing.md),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HomeNavButton — white button with FA icon + label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeNavButton(
    label: String,
    icon: String,
    iconFamily: FaFamily = FaFamily.Regular,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    Button(
        onClick = onClick,
        modifier = modifier.height(spacing.homeButtonMinHeight),
        colors = ButtonDefaults.buttonColors(containerColor = White),
    ) {
        FaIcon(
            name     = icon,
            family   = iconFamily,
            size     = spacing.iconMd,
            tint     = Crimson,
            modifier = Modifier.padding(end = 6.dp),
        )
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Navy)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SocialLink — icon + text row, tappable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SocialLink(
    label: String,
    icon: String,
    iconFamily: FaFamily = FaFamily.Brands,
    onClick: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        FaIcon(name = icon, family = iconFamily, size = spacing.iconMd, tint = Crimson)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Crimson,
        )
    }
}
