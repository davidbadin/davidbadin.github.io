package sk.punkacidetom.pd2026.feature.news

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60
import java.io.File

@Composable
fun NewsletterVolumeScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsletterVolumeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalAppSpacing.current

    Column(modifier = modifier.fillMaxSize().background(Navy)) {
        Text(
            text = stringResource(R.string.newsletter_volume, uiState.volumeId),
            style = MaterialTheme.typography.displayMedium,
            color = White,
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
        )
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Crimson)
            }
            uiState.error != null -> Text(
                text = stringResource(R.string.newsletter_error),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60,
                modifier = Modifier.padding(spacing.md),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.sm),
            ) {
                uiState.pagePaths.forEach { path ->
                    ZoomableNewsletterPage(
                        filePath = path,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))
                }
            }
        }
    }
}

/**
 * Single newsletter page image with pinch-to-zoom (1×–5×) and pan support.
 * Reset by pinching back to 1×.
 */
@Composable
private fun ZoomableNewsletterPage(filePath: String, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        // Clamp pan so image cannot be dragged off-screen at scale=1
        if (scale > 1f) offset += panChange else offset = Offset.Zero
    }

    Box(modifier = modifier.transformable(state = transformState), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = File(filePath),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
        )
    }
}
