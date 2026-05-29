package sk.punkacidetom.pd2026.feature.spotify

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.feature.spotify.util.SpotifyLauncher

/**
 * Shared Spotify player composable.
 *
 * Shows a Spotify iframe embed (WebView) and an "Open in Spotify" button.
 * When the Spotify app is installed, the button deep-links directly into it.
 * When not installed, Chrome Custom Tabs opens the web player.
 *
 * The WebView height adapts dynamically to the embedded content height (min 80dp),
 * so the enclosing Box never wastes space when the compact player is shown.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SpotifyPlayerComposable(
    embedUrl: String,
    openLabel: String = stringResource(sk.punkacidetom.pd2026.feature.spotify.R.string.spotify_open_app),
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier,
    embedHeight: Int = 152, // kept for API compatibility; actual height is measured dynamically
) {
    val spacing = LocalAppSpacing.current
    var webHeight by remember { mutableStateOf(80) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(webHeight.dp.coerceAtLeast(80.dp))
                .clip(RoundedCornerShape(spacing.cardCorner))
                .background(NavyLight),
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun reportHeight(h: Int) {
                                    webHeight = h.coerceAtLeast(80)
                                }
                            },
                            "Android",
                        )
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                view.evaluateJavascript(
                                    "(function() { Android.reportHeight(document.body.scrollHeight); })();",
                                ) {}
                            }
                        }
                        loadUrl(embedUrl)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(spacing.sm))
        Button(
            onClick = onOpenClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Crimson),
        ) {
            Text(
                text = openLabel,
                style = MaterialTheme.typography.labelLarge,
                color = White,
            )
        }
    }
}

// Generates the Spotify embed URL from a playlist or artist ID
fun spotifyPlaylistEmbedUrl(playlistId: String) =
    "https://open.spotify.com/embed/playlist/$playlistId?utm_source=generator&theme=0"

fun spotifyArtistEmbedUrl(artistId: String) =
    "https://open.spotify.com/embed/artist/$artistId?utm_source=generator&theme=0"
