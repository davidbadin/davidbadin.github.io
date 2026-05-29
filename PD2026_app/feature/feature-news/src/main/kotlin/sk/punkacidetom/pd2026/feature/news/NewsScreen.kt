package sk.punkacidetom.pd2026.feature.news

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

private const val FACEBOOK_URL = "https://www.facebook.com/punkacidetom"

@Composable
fun NewsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Open Facebook page directly in Chrome Custom Tab on first composition.
    // No intermediate screen is needed — the Custom Tab provides the full page experience.
    LaunchedEffect(Unit) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(context, Uri.parse(FACEBOOK_URL))
    }
}
