package sk.punkacidetom.pd2026.feature.news

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** Mobile Facebook page — lighter and less aggressive about redirecting to the app. */
private const val FACEBOOK_URL = "https://m.facebook.com/punkacidetom"

/** Chrome mobile UA so Facebook does not block the WebView. */
private const val CHROME_UA =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
    "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

/**
 * Loads the festival Facebook page in an embedded WebView so the app header/footer
 * remain visible (unlike a Chrome Custom Tab which runs outside the scaffold).
 *
 * Spoofs the User-Agent so Facebook does not block the WebView, and intercepts
 * fb:// / intent:// deep-links to forward them to the Facebook app (or silently
 * ignore if not installed) instead of crashing the WebView.
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
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(false)
                settings.userAgentString = CHROME_UA
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        val url = request.url
                        return if (url.scheme == "fb" || url.scheme == "intent") {
                            // Forward to Facebook app; silently ignore if not installed
                            try {
                                view.context.startActivity(
                                    Intent(Intent.ACTION_VIEW, url)
                                )
                            } catch (_: ActivityNotFoundException) { }
                            true   // consumed — do not load in WebView
                        } else {
                            false  // let WebView handle normal https:// URLs
                        }
                    }
                }
                loadUrl(FACEBOOK_URL)
            }
        },
    )
}
