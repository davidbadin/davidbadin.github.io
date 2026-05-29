package sk.punkacidetom.pd2026.feature.news

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NewsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val spacing = LocalAppSpacing.current

    // Choose locale-specific HTML asset
    val localeTag = context.resources.configuration.locales[0].language
    val assetFile = if (localeTag.startsWith("en")) "news_en.html" else "news_sk.html"

    val webView = remember {
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadUrl("file:///android_asset/$assetFile")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
        }

        // Fallback button shown below the WebView
        Button(
            onClick = {
                CustomTabsIntent.Builder().build()
                    .launchUrl(context, Uri.parse("https://www.facebook.com/punkacidetom"))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            colors = ButtonDefaults.buttonColors(containerColor = Crimson),
        ) {
            Text(
                text = stringResource(R.string.news_open_facebook),
                style = MaterialTheme.typography.labelLarge,
                color = White,
            )
        }
    }
}
