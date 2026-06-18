package sk.punkacidetom.pd2026.feature.spotify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.feature.spotify.util.SpotifyLauncher

private const val FESTIVAL_PLAYLIST_ID = "5QL8HJ0cWaLGS2Qxby0xDG"

@Composable
fun SpotifyScreen(
    modifier: Modifier = Modifier,
    viewModel: SpotifyViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalAppSpacing.current

    LaunchedEffect(Unit) {
        viewModel.connect("spotify:playlist:$FESTIVAL_PLAYLIST_ID")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
    ) {
        Text(
            text = stringResource(R.string.spotify_title),
            style = MaterialTheme.typography.displayMedium,
            color = White,
        )
        Spacer(modifier = Modifier.height(spacing.md))
        Text(
            text = stringResource(R.string.spotify_playlist_label),
            style = MaterialTheme.typography.headlineSmall,
            color = White,
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        SpotifyPlayerComposable(
            uiState = uiState,
            embedUrl = spotifyPlaylistEmbedUrl(FESTIVAL_PLAYLIST_ID),
            onOpenClick = { SpotifyLauncher.openPlaylist(context, FESTIVAL_PLAYLIST_ID) },
            onTogglePlayPause = viewModel::togglePlayPause,
            onSkipNext = viewModel::skipNext,
            onSkipPrevious = viewModel::skipPrevious,
        )
    }
}
