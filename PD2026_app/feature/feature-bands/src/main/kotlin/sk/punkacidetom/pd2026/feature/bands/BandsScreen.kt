package sk.punkacidetom.pd2026.feature.bands

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.Stages
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.PARALLAX_SCROLL_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_LOGO_WIDTH_FRACTION
import sk.punkacidetom.pd2026.core.ui.theme.SHORT_HEADER_TITLE_TOP_PADDING_DP
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

@Composable
fun BandsScreen(
    onBandClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BandsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value
    val spacing = LocalAppSpacing.current
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize().background(Navy)) {

        // Parallax background — outside the scroll column
        AsyncImage(
            model = "file:///android_asset/header_main.png",
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = -scrollState.value * PARALLAX_SCROLL_FRACTION },
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo
            Spacer(modifier = Modifier.height(SHORT_HEADER_LOGO_TOP_PADDING_DP.dp))
            AsyncImage(
                model = "file:///android_asset/logo_pd_short.png",
                contentDescription = "Punkáči deťom 2026",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(SHORT_HEADER_LOGO_WIDTH_FRACTION),
            )
            // Title
            Spacer(modifier = Modifier.height(SHORT_HEADER_TITLE_TOP_PADDING_DP.dp))
            Text(
                text = stringResource(R.string.bands_title),
                style = MaterialTheme.typography.displayMedium,
                color = White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md),
            )

            // Navy-backed band list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy),
            ) {
                if (uiState.bands.isEmpty()) {
                    Text(
                        text = stringResource(R.string.bands_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteAlpha60,
                        modifier = Modifier.padding(spacing.md),
                    )
                } else {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    uiState.bands.forEach { band ->
                        BandRow(
                            band = band,
                            isFavourite = uiState.favouriteIds.contains(band.id),
                            onBandClick = { onBandClick(band.id) },
                            onToggleFavourite = { viewModel.toggleFavourite(band.id) },
                        )
                        Spacer(modifier = Modifier.height(spacing.sm))
                    }
                }
            }
        }
    }
}

@Composable
private fun BandRow(
    band: Band,
    isFavourite: Boolean,
    onBandClick: () -> Unit,
    onToggleFavourite: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val dayStr  = "${band.startDate.dayOfMonth}. ${band.startDate.monthValue}."
    val timeStr = "${band.startTime.hour}:${band.startTime.minute.toString().padStart(2, '0')}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cardCorner))
            .background(NavyLight)
            .clickable(onClick = onBandClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = band.name,
                style = MaterialTheme.typography.titleMedium,
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    text = Stages.displayName(band.stageCode),
                    style = MaterialTheme.typography.labelSmall,
                    color = Crimson,
                )
                if (band.genre.isNotBlank()) {
                    Text(
                        text = band.genre,
                        style = MaterialTheme.typography.labelSmall,
                        color = WhiteAlpha60,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = "$dayStr $timeStr",
                style = MaterialTheme.typography.labelSmall,
                color = WhiteAlpha60,
            )
        }
        IconButton(onClick = onToggleFavourite) {
            Icon(
                imageVector = if (isFavourite) Icons.Filled.Favorite
                              else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavourite) Crimson else WhiteAlpha60,
                modifier = Modifier.size(spacing.iconMd),
            )
        }
    }
}
