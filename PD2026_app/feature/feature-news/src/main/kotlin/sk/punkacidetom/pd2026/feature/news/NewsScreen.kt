package sk.punkacidetom.pd2026.feature.news

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val FACEBOOK_URL = "https://www.facebook.com/punkacidetom"

/**
 * Loads the festival Facebook page in an embedded WebView so the app header/footer
 * remain visible (unlike a Chrome Custom Tab which runs outside the scaffold).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NewsScreen(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadUrl(FACEBOOK_URL)
            }
        },
    )
}
