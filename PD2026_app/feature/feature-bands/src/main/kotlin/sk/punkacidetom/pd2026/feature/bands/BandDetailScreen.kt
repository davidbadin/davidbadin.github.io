package sk.punkacidetom.pd2026.feature.bands

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.roundToInt
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.Stages
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.PARALLAX_SCROLL_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_BOTTOM_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60
import sk.punkacidetom.pd2026.feature.spotify.SpotifyWebViewCard
import sk.punkacidetom.pd2026.feature.spotify.spotifyArtistEmbedUrl
import sk.punkacidetom.pd2026.feature.spotify.util.SpotifyLauncher
import java.time.format.TextStyle
import java.util.Locale

// ---------------------------------------------------------------------------
// Image source fallback chain:
//   NetworkPng → NetworkJpg → AssetPng → AssetJpg → None (show logo)
// ---------------------------------------------------------------------------

private enum class ImageSource { NetworkPng, NetworkJpg, AssetPng, AssetJpg, None }

private fun Band.imageUri(source: ImageSource): String = when (source) {
    ImageSource.NetworkPng -> bandImagePngUrl
    ImageSource.NetworkJpg -> bandImageJpgUrl
    ImageSource.AssetPng   -> "file:///android_asset/bands/$imageName.png"
    ImageSource.AssetJpg   -> "file:///android_asset/bands/$imageName.jpg"
    ImageSource.None       -> ""
}

private fun ImageSource.next(): ImageSource = when (this) {
    ImageSource.NetworkPng -> ImageSource.NetworkJpg
    ImageSource.NetworkJpg -> ImageSource.AssetPng
    ImageSource.AssetPng   -> ImageSource.AssetJpg
    ImageSource.AssetJpg   -> ImageSource.None
    ImageSource.None       -> ImageSource.None
}

// ---------------------------------------------------------------------------
// Crop / fade constants (fraction of the image's rendered pixel height H(i))
// ---------------------------------------------------------------------------

private const val CR_TOP    = 0.32f   // 32% of H(i) hidden at top
private const val CR_BOTTOM = 0.06f   // 6%  of H(i) hidden at bottom
private const val FADE_TOP  = 0.15f   // 15% of H(i) — top fade height
private const val FADE_BOT  = 0.10f   // 10% of H(i) — bottom fade height
// Visible slice = (1 - CR_TOP - CR_BOTTOM) = 0.62 × H(i)

// ---------------------------------------------------------------------------

@Composable
fun BandDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BandDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalAppSpacing.current
    val context = LocalContext.current
    val band = uiState.band

    // True when the band record has a photo (imageName non-blank).
    val hasPhoto = band != null && band.imageName.isNotBlank()

    val scrollState = rememberScrollState()

    // Lifted from BandPhotoBackground so the Column spacer height can be computed here.
    // Defaults to 1:1 aspect ratio; updated via callback when the image loads.
    var imgIntrinsicWidth  by remember(band?.imageName) { mutableIntStateOf(1080) }
    var imgIntrinsicHeight by remember(band?.imageName) { mutableIntStateOf(1080) }

    Scaffold(containerColor = Navy) { innerPadding ->
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(Navy)
                .padding(innerPadding),
        ) {
            val density = LocalDensity.current
            val screenWidthPx = constraints.maxWidth.toFloat()

            // Visible height of the band photo after cropping (H(c) = H(i) * (1 - CR_TOP - CR_BOTTOM)).
            // Defaults to a reasonable height before the image loads (square-aspect fallback).
            // The Column spacer reserves this exact height so content starts below the photo.
            val bandImageVisibleHeightDp = with(density) {
                (screenWidthPx * imgIntrinsicHeight.toFloat() / imgIntrinsicWidth.toFloat() *
                    (1f - CR_TOP - CR_BOTTOM)).toDp()
            }

            // ── Background layer (outside the scrollable Column) ─────────────────
            if (hasPhoto && band != null) {
                // Band photo with same crop/fade logic as before, now with parallax.
                BandPhotoBackground(
                    band = band,
                    scrollOffset = scrollState.value,
                    screenWidthPx = screenWidthPx,
                    onIntrinsicSize = { w, h ->
                        imgIntrinsicWidth  = w
                        imgIntrinsicHeight = h
                    },
                )
            } else {
                // Generic festival header — same pattern as News, Tickets, Settings.
                AsyncImage(
                    model = "file:///android_asset/header_main.png",
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = -scrollState.value * PARALLAX_SCROLL_FRACTION
                        },
                    alignment = Alignment.TopCenter,
                )
            }

            // ── Scrollable content column ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // Header area — sized by its content; clipped so nothing bleeds out.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clipToBounds(),
                ) {
                    if (hasPhoto) {
                        // Reserve the same height as the visible cropped band photo.
                        // This placeholder moves at 1× scroll speed; the photo behind
                        // it moves at 0.5×, creating the parallax effect.
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(bandImageVisibleHeightDp),
                        )
                    } else {
                        // Short header: logo + band name as title + genre.
                        // Matches the pattern used by BandsScreen, NewsScreen, etc.
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
                                text = band?.name ?: "",
                                style = MaterialTheme.typography.displayMedium,
                                color = White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.md),
                            )
                            if (band != null && band.genre.isNotBlank()) {
                                Text(
                                    text = band.genre,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Crimson,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = spacing.md),
                                )
                            }
                            Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_BOTTOM_PADDING_DP.dp))
                        }
                    }
                }

                // ── Screen content ───────────────────────────────────────────────
                if (band == null) {
                    Text(
                        text = "…",
                        color = WhiteAlpha60,
                        modifier = Modifier.padding(spacing.md),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Day / date + time + stage  |  Favourite icon
                        // NOTE: favourite icon is on the RIGHT, vertically aligned with the
                        // "dayName, dateStr" line (first line of the left column).
                        val dayName = band.startDate.dayOfWeek
                            .getDisplayName(TextStyle.FULL, Locale.forLanguageTag(uiState.language))
                            .replaceFirstChar { it.uppercase() }
                        val dateStr = "${band.startDate.dayOfMonth}. " +
                            "${band.startDate.monthValue}. ${band.startDate.year}"
                        val timeStr = "${band.startTime.hour}:" +
                            "${band.startTime.minute.toString().padStart(2, '0')}" +
                            " – ${band.endTime.hour}:" +
                            "${band.endTime.minute.toString().padStart(2, '0')}"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Spacer(modifier = Modifier.height(spacing.sm))
                                Text(
                                    text = "$dayName, $dateStr",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WhiteAlpha60,
                                )
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = White,
                                )
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    text = Stages.displayName(band.stageCode),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WhiteAlpha60,
                                )
                                Spacer(modifier = Modifier.height(spacing.md))
                            }
                            IconButton(onClick = { viewModel.toggleFavourite() }) {
                                Icon(
                                    imageVector = if (uiState.isFavourite) Icons.Filled.Favorite
                                                  else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (uiState.isFavourite) Crimson else WhiteAlpha60,
                                    modifier = Modifier.size(spacing.iconLg),
                                )
                            }
                        }

                        // Description
                        val description = band.description(uiState.language)
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = White,
                            )
                        }

                        // Spotify player
                        if (band.spotifyArtistId.isNotBlank()) {
                            Spacer(modifier = Modifier.height(spacing.md))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                SpotifyWebViewCard(
                                    embedUrl = spotifyArtistEmbedUrl(band.spotifyArtistId),
                                )
                            }
                            Spacer(modifier = Modifier.height(spacing.sm))
                            Button(
                                onClick = { SpotifyLauncher.openArtist(context, band.spotifyArtistId) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                            ) {
                                Text(
                                    text = "Spotify",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = White,
                                )
                            }
                        }

                        // Bottom spacer
                        Spacer(modifier = Modifier.height(spacing.xl))
                    }
                }
            }

            // ── Back button — static, never scrolls away ─────────────────────────
            // Placed AFTER the Column in the BoxWithConstraints z-order so it
            // renders on top of the scrollable content.
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(spacing.sm)
                    .align(Alignment.TopStart),
            ) {
                FaIcon(name = "arrow-left", size = spacing.iconLg, tint = White)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BandPhotoBackground
//
// Band photo displayed as a parallax background layer.
// Crop/fade logic is identical to the old BandHeaderImage photo branch.
// [scrollOffset] is the current scroll position in pixels (from rememberScrollState).
// [screenWidthPx] is the container width in pixels (from BoxWithConstraints.constraints.maxWidth).
// [onIntrinsicSize] is called once on successful image load with (width, height) in pixels.
// ---------------------------------------------------------------------------

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun BandPhotoBackground(
    band: Band,
    scrollOffset: Int,
    screenWidthPx: Float,
    onIntrinsicSize: (width: Int, height: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var source by remember(band.imageName) {
        mutableStateOf(ImageSource.NetworkPng)
    }
    var imgIntrinsicWidth  by remember(band.imageName) { mutableIntStateOf(1080) }
    var imgIntrinsicHeight by remember(band.imageName) { mutableIntStateOf(1080) }

    val density = LocalDensity.current

    val renderedHeightPx  = screenWidthPx * imgIntrinsicHeight.toFloat() / imgIntrinsicWidth.toFloat()
    val containerHeightPx = renderedHeightPx * (1f - CR_TOP - CR_BOTTOM)
    val containerHeightDp = with(density) { containerHeightPx.toDp() }
    val fadeTopHeightDp   = with(density) { (renderedHeightPx * FADE_TOP).toDp() }
    val fadeBotHeightDp   = with(density) { (renderedHeightPx * FADE_BOT).toDp() }

    val imageUrl = if (source != ImageSource.None) band.imageUri(source) else ""

    // The outer Box is the visible window (H(c) tall) and moves at parallax speed.
    // clipToBounds() confines the image crop to this window.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeightDp)
            .clipToBounds()
            .graphicsLayer {
                translationY = -scrollOffset * PARALLAX_SCROLL_FRACTION
            },
    ) {
        if (imageUrl.isNotBlank()) {
            // Band photo — uses the same layout{} crop trick as the old BandHeaderImage.
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .listener(
                        onError = { _, _ -> source = source.next() },
                        onSuccess = { _, result ->
                            val w = result.drawable.intrinsicWidth.coerceAtLeast(1)
                            val h = result.drawable.intrinsicHeight.coerceAtLeast(1)
                            imgIntrinsicWidth  = w
                            imgIntrinsicHeight = h
                            onIntrinsicSize(w, h)
                        },
                    )
                    .crossfade(true)
                    .build(),
                contentDescription = band.name,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
                        )
                        val hi      = if (placeable.height > 0) placeable.height
                                      else constraints.maxWidth
                        val cropTop = (hi * CR_TOP).roundToInt()
                        val hc      = hi - cropTop - (hi * CR_BOTTOM).roundToInt()
                        layout(placeable.width, hc.coerceAtLeast(1)) {
                            placeable.placeRelative(0, -cropTop)
                        }
                    },
            )

            // Top fade: Navy → Transparent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeTopHeightDp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Navy, Color.Transparent))),
            )

            // Bottom fade: Transparent → Navy
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeBotHeightDp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Navy))),
            )
        }
    }
}
