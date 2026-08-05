package sk.punkacidetom.pd2026.feature.spotify

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing

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
        loading="lazy"
        onload="Android.onIframeLoaded()">
      </iframe>
    </body>
    </html>
""".trimIndent()

@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
@Composable
fun SpotifyWebViewCard(embedUrl: String) {
    val spacing = LocalAppSpacing.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    DisposableEffect(embedUrl) {
        onDispose {
            webViewRef?.let { wv ->
                wv.loadUrl("about:blank")
                wv.destroy()
                webViewRef = null
            }
            isLoaded = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
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
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onIframeLoaded() { isLoaded = true }
                        },
                        "Android",
                    )
                    webViewClient = WebViewClient()
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
// URL helpers
// ---------------------------------------------------------------------------

fun spotifyPlaylistEmbedUrl(playlistId: String) =
    "https://open.spotify.com/embed/playlist/$playlistId?utm_source=generator&theme=0"

fun spotifyArtistEmbedUrl(artistId: String) =
    "https://open.spotify.com/embed/artist/$artistId?utm_source=generator&theme=0"
