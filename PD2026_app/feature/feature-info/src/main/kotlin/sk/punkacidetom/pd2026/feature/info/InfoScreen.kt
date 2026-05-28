package sk.punkacidetom.pd2026.feature.info

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import sk.punkacidetom.pd2026.core.ui.theme.Navy

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    viewModel: InfoViewModel = hiltViewModel(),
) {
    val html by viewModel.htmlContent.collectAsState()
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView, request: WebResourceRequest
                ): Boolean {
                    // Open external links in Chrome Custom Tabs
                    if (request.url.scheme == "http" || request.url.scheme == "https") {
                        CustomTabsIntent.Builder().build()
                            .launchUrl(context, request.url)
                        return true
                    }
                    return false
                }
            }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Navy),
    ) {
        html?.let { content ->
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize(),
                update = { wv ->
                    wv.loadDataWithBaseURL(
                        "https://davidbadin.github.io/PD2026_app/",
                        content,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                },
            )
        }
    }
}
