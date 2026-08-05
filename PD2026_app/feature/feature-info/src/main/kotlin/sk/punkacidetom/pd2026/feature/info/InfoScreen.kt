package sk.punkacidetom.pd2026.feature.info

import android.webkit.WebResourceRequest
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_BOTTOM_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val context = LocalContext.current

    val infoHtml = remember {
        context.assets.open("info.html").bufferedReader().readText()
    }

    Scaffold(
        containerColor = Navy,
    ) { innerPadding ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Navy)
                .padding(innerPadding),
        ) {

            // Part 1 — Native Compose header (logo + title), fixed at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                AsyncImage(
                    model = "file:///android_asset/header_main.png",
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.matchParentSize(),
                    alignment = Alignment.TopCenter,
                )
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
                        text = stringResource(R.string.info_title),
                        style = MaterialTheme.typography.displayMedium,
                        color = White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md),
                    )
                    Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_BOTTOM_PADDING_DP.dp))
                }
            }

            // Part 2 — WebView fills remaining height; handles its own scrolling
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest,
                            ): Boolean {
                                if (request.url.scheme == "http" || request.url.scheme == "https") {
                                    CustomTabsIntent.Builder().build()
                                        .launchUrl(ctx, request.url)
                                    return true
                                }
                                return false
                            }
                        }
                        settings.javaScriptEnabled = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                update = { wv ->
                    wv.loadDataWithBaseURL(
                        "file:///android_asset/",
                        infoHtml,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                },
            )
        }
    }
}
