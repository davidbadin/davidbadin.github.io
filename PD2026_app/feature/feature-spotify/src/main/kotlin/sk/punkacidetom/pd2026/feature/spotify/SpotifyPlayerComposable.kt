package sk.punkacidetom.pd2026.feature.spotify

import android.annotation.SuppressLint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.spotify.protocol.types.PlayerState
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

// Spotify brand green — only used for the SDK status indicator
private val SpotifyGreen = Color(0xFF1DB954)

/**
 * Spotify player composable that adapts to three states:
 *
 * - [SpotifyUiState.Connecting]      — spinner while the SDK handshake is in progress
 * - [SpotifyUiState.SdkConnected]    — native controls (track name, artist, play/pause, skip)
 * - [SpotifyUiState.FallbackWebView] — Spotify iframe embed via WebView (Spotify not installed / SDK failed)
 *
 * An "Open in Spotify" button is shown in every state.
 */
@Composable
fun SpotifyPlayerComposable(
    uiState: SpotifyUiState,
    embedUrl: String,
    onOpenClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current

    Column(modifier = modifier.fillMaxWidth()) {
        when (uiState) {
            SpotifyUiState.Connecting -> SpotifyConnectingCard()
            is SpotifyUiState.SdkConnected -> SpotifySdkPlayerCard(
                playerState = uiState.playerState,
                onTogglePlayPause = onTogglePlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
            )
            SpotifyUiState.FallbackWebView -> SpotifyWebViewCard(embedUrl = embedUrl)
        }

        Spacer(modifier = Modifier.height(spacing.sm))

        Button(
            onClick = onOpenClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Crimson),
        ) {
            Text(
                text = stringResource(R.string.spotify_open_app),
                style = MaterialTheme.typography.labelLarge,
                color = White,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// SDK: native player controls
// ---------------------------------------------------------------------------

@Composable
private fun SpotifySdkPlayerCard(
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val track = playerState.track

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cardCorner))
            .background(NavyLight)
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        // Connected status dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(SpotifyGreen),
            )
            Text(
                text = stringResource(R.string.spotify_sdk_connected),
                style = MaterialTheme.typography.labelSmall,
                color = SpotifyGreen,
                modifier = Modifier.padding(start = 6.dp),
            )
        }

        // Track / artist
        if (track != null) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleMedium,
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist.name,
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = stringResource(R.string.spotify_no_track),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60,
            )
        }

        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onSkipPrevious) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_previous),
                    contentDescription = stringResource(R.string.spotify_skip_previous),
                    tint = White,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Crimson),
            ) {
                Icon(
                    painter = painterResource(
                        if (playerState.isPaused) android.R.drawable.ic_media_play
                        else android.R.drawable.ic_media_pause,
                    ),
                    contentDescription = stringResource(
                        if (playerState.isPaused) R.string.spotify_play else R.string.spotify_pause,
                    ),
                    tint = White,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onSkipNext) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_next),
                    contentDescription = stringResource(R.string.spotify_skip_next),
                    tint = White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Fallback: WebView iframe embed
// ---------------------------------------------------------------------------

// Embed height shared between the HTML template and the Compose Box
private const val SPOTIFY_EMBED_HEIGHT_DP = 152

private fun spotifyEmbedHtml(embedUrl: String, heightDp: Int): String = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { background: transparent; overflow: hidden; }
        iframe { display: block; width: 100%; height: ${heightDp}px; border: none; }
      </style>
    </head>
    <body>
      <iframe
        src="$embedUrl"
        allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
        loading="lazy">
      </iframe>
    </body>
    </html>
""".trimIndent()

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SpotifyWebViewCard(embedUrl: String) {
    val spacing = LocalAppSpacing.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoaded by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    DisposableEffect(embedUrl) {
        onDispose {
            webViewRef?.let { wv ->
                wv.onPause()
                wv.pauseTimers()
                wv.loadUrl("about:blank")
                wv.destroy()
                webViewRef = null
            }
            isLoaded = false
            hasError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isLoaded) Modifier
                    .height(SPOTIFY_EMBED_HEIGHT_DP.dp)
                    .clip(RoundedCornerShape(spacing.cardCorner))
                else Modifier.height(0.dp)
            ),
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            if (!hasError && url != "about:blank") {
                                isLoaded = true
                            }
                        }

                        @Suppress("OVERRIDE_DEPRECATION")
                        override fun onReceivedError(
                            view: WebView,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?,
                        ) {
                            hasError = true
                            isLoaded = false
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError,
                        ) {
                            if (request.isForMainFrame) {
                                hasError = true
                                isLoaded = false
                            }
                        }
                    }
                    loadDataWithBaseURL(
                        "https://open.spotify.com/",
                        spotifyEmbedHtml(embedUrl, SPOTIFY_EMBED_HEIGHT_DP),
                        "text/html",
                        "UTF-8",
                        null,
                    )
                }.also { webViewRef = it }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(SPOTIFY_EMBED_HEIGHT_DP.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Connecting: spinner placeholder
// ---------------------------------------------------------------------------

@Composable
private fun SpotifyConnectingCard() {
    val spacing = LocalAppSpacing.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(spacing.cardCorner))
            .background(NavyLight),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SpotifyGreen)
    }
}

// ---------------------------------------------------------------------------
// URL helpers
// ---------------------------------------------------------------------------

fun spotifyPlaylistEmbedUrl(playlistId: String) =
    "https://open.spotify.com/embed/playlist/$playlistId?utm_source=generator&theme=0"

fun spotifyArtistEmbedUrl(artistId: String) =
    "https://open.spotify.com/embed/artist/$artistId?utm_source=generator&theme=0"
