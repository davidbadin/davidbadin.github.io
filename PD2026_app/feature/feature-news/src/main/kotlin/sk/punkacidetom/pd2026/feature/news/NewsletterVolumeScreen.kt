package sk.punkacidetom.pd2026.feature.news

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
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

    var fullscreenIndex by remember { mutableIntStateOf(-1) }

    BackHandler(enabled = fullscreenIndex >= 0) {
        fullscreenIndex = -1
    }

    Box(modifier = modifier.fillMaxSize().background(Navy)) {
        // ── Screen title + content ─────────────────────────────────────────
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Crimson)
            }
            uiState.error != null -> {
                Text(
                    text = stringResource(R.string.newsletter_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteAlpha60,
                    modifier = Modifier.padding(spacing.md),
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = spacing.md,
                    vertical = spacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.newsletter_volume, uiState.volumeId),
                        style = MaterialTheme.typography.displayMedium,
                        color = White,
                        modifier = Modifier.padding(bottom = spacing.sm),
                    )
                }
                itemsIndexed(uiState.pagePaths) { index, path ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(path))
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .clip(RoundedCornerShape(spacing.cardCorner))
                                .clickable { fullscreenIndex = index },
                        )
                    }
                }
            }
        }

        // ── Level 2: fullscreen zoomable viewer ───────────────────────────
        AnimatedVisibility(
            visible = fullscreenIndex >= 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            val index = fullscreenIndex

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                if (index >= 0 && index < uiState.pagePaths.size) {
                    ZoomableAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(uiState.pagePaths[index]))
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        state = rememberZoomableImageState(
                            rememberZoomableState(
                                zoomSpec = ZoomSpec(maxZoomFactor = 4f),
                            )
                        ),
                        contentScale = ContentScale.Fit,
                    )
                }

                IconButton(
                    onClick = { fullscreenIndex = -1 },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
