package sk.punkacidetom.pd2026.feature.bands

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.Stages
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60
import sk.punkacidetom.pd2026.feature.spotify.SpotifyPlayerComposable
import sk.punkacidetom.pd2026.feature.spotify.SpotifyViewModel
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

@Composable
fun BandDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BandDetailViewModel = hiltViewModel(),
    spotifyViewModel: SpotifyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spotifyState by spotifyViewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalAppSpacing.current
    val context = LocalContext.current
    val band = uiState.band

    // True when the band record has a photo (imageName non-blank).
    // Used to switch layout: image present → hide text name/genre, show compact favourite row.
    val hasPhoto = band != null && band.imageName.isNotBlank()

    LaunchedEffect(band?.spotifyArtistId) {
        val artistId = band?.spotifyArtistId
        if (!artistId.isNullOrBlank()) {
            spotifyViewModel.connect("spotify:artist:$artistId")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Navy),
    ) {
        // Header image + back button
        item(key = "header") {
            val headerHeight = if (hasPhoto) spacing.bandImageHeight else spacing.bandImageHeight * 0.45f
            Box {
                BandHeaderImage(
                    band = band,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight),
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(spacing.sm)
                        .align(Alignment.TopStart),
                ) {
                    FaIcon(name = "arrow-left", size = spacing.iconLg, tint = White)
                }
            }
        }

        if (band == null) {
            item(key = "loading") {
                Text(
                    text = "…",
                    color = WhiteAlpha60,
                    modifier = Modifier.padding(spacing.md),
                )
            }
            return@LazyColumn
        }

        // Name + favourite toggle
        item(key = "name") {
            if (hasPhoto) {
                // Image already contains the band name — only show the favourite toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xs, end = spacing.xs),
                    horizontalArrangement = Arrangement.End,
                ) {
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
            } else {
                // No image — show name text + favourite toggle side-by-side
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.sm, start = spacing.md, end = spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = band.name,
                        style = MaterialTheme.typography.displaySmall,
                        color = White,
                        modifier = Modifier.weight(1f),
                    )
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
            }
        }

        // Genre — hidden when an image is present (name is visible in the photo)
        if (band.genre.isNotBlank() && !hasPhoto) {
            item(key = "genre") {
                Text(
                    text = band.genre,
                    style = MaterialTheme.typography.titleMedium,
                    color = Crimson,
                    modifier = Modifier.padding(horizontal = spacing.md),
                )
            }
        }

        // Day / time / stage
        item(key = "datetime") {
            val dayName = band.startDate.dayOfWeek
                .getDisplayName(TextStyle.FULL, Locale.forLanguageTag(uiState.language))
                .replaceFirstChar { it.uppercase() }
            val dateStr = "${band.startDate.dayOfMonth}. ${band.startDate.monthValue}. ${band.startDate.year}"
            val timeStr = "${band.startTime.hour}:${band.startTime.minute.toString().padStart(2, '0')}" +
                " – ${band.endTime.hour}:${band.endTime.minute.toString().padStart(2, '0')}"
            Column(modifier = Modifier.padding(horizontal = spacing.md)) {
                Spacer(modifier = Modifier.height(spacing.sm))
                Text(text = "$dayName, $dateStr", style = MaterialTheme.typography.bodyMedium, color = WhiteAlpha60)
                Text(text = timeStr, style = MaterialTheme.typography.titleSmall, color = White)
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(text = Stages.displayName(band.stageCode), style = MaterialTheme.typography.bodyMedium, color = WhiteAlpha60)
                Spacer(modifier = Modifier.height(spacing.md))
            }
        }

        // Description — in its own item so long text never bloats a shared layer
        val description = band.description(uiState.language)
        if (description.isNotBlank()) {
            item(key = "description") {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    modifier = Modifier.padding(horizontal = spacing.md),
                )
            }
        }

        // Spotify player
        if (band.spotifyArtistId.isNotBlank()) {
            item(key = "spotify") {
                Spacer(modifier = Modifier.height(spacing.md))
                SpotifyPlayerComposable(
                    uiState = spotifyState,
                    embedUrl = spotifyArtistEmbedUrl(band.spotifyArtistId),
                    onOpenClick = { SpotifyLauncher.openArtist(context, band.spotifyArtistId) },
                    onTogglePlayPause = spotifyViewModel::togglePlayPause,
                    onSkipNext = spotifyViewModel::skipNext,
                    onSkipPrevious = spotifyViewModel::skipPrevious,
                    modifier = Modifier.padding(horizontal = spacing.md),
                )
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(spacing.xl))
        }
    }
}

@Composable
private fun BandHeaderImage(band: Band?, modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current

    // Walk the fallback chain on each Coil onError callback
    var source by remember(band?.imageName) {
        mutableStateOf(
            if (band != null && band.imageName.isNotBlank()) ImageSource.NetworkPng
            else ImageSource.None
        )
    }
    val imageUrl = if (band != null && source != ImageSource.None) band.imageUri(source) else ""

    Box(modifier = modifier) {
        if (imageUrl.isNotBlank()) {
            // Photo found — crop from top so faces/subjects near the bottom stay visible
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .listener(onError = { _, _ -> source = source.next() })
                    .crossfade(true)
                    .build(),
                contentDescription = band?.name,
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter,   // crop excess from the top
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        // Also crop the bottom 5% of the rendered image
                        clipRect(bottom = size.height * 0.95f) {
                            drawContent()
                        }
                    },
            )

            // Top gradient: Navy → Transparent (blends into the page background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.bandImageHeight * 0.35f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Navy, Color.Transparent),
                        )
                    ),
            )
        } else {
            // No band-specific image — show the festival logo centred on the Navy background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Navy),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/logo_pd.png")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth(0.55f),
                )
            }
        }

        // Bottom gradient: Transparent → Navy (always shown)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.bandImageHeight * 0.25f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Navy),
                    )
                ),
        )
    }
}
