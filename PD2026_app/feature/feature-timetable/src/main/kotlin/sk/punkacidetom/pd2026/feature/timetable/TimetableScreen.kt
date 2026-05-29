package sk.punkacidetom.pd2026.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.Stages
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TimetableScreen(
    onBandClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimetableViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalAppSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy),
    ) {
        Text(
            text = stringResource(R.string.timetable_title),
            style = MaterialTheme.typography.displayMedium,
            color = White,
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
        )

        // Day tab selector
        if (uiState.days.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                uiState.days.forEachIndexed { index, day ->
                    val dayName = day.date.dayOfWeek
                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                        .replaceFirstChar { it.uppercase() }
                    val selected = index == uiState.selectedDayIndex
                    TextButton(
                        onClick = { viewModel.selectDay(index) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cardCorner))
                            .background(if (selected) Crimson else NavyLight),
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) White else WhiteAlpha60,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.sm))

        // Stage headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md),
        ) {
            Text(
                text = Stages.displayName("A"),
                style = MaterialTheme.typography.labelLarge,
                color = Crimson,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Text(
                text = Stages.displayName("B"),
                style = MaterialTheme.typography.labelLarge,
                color = Crimson,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(spacing.xs))

        // Two-column timetable
        if (uiState.stageABands.isEmpty() && uiState.stageBBands.isEmpty()) {
            Text(
                text = stringResource(R.string.timetable_no_slots),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60,
                modifier = Modifier.padding(spacing.md),
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    uiState.stageABands.forEach { band ->
                        SlotCard(
                            band = band,
                            isFavourite = uiState.favouriteIds.contains(band.id),
                            onClick = { onBandClick(band.id) },
                        )
                        Spacer(modifier = Modifier.height(spacing.sm))
                    }
                }
                Spacer(modifier = Modifier.width(spacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    uiState.stageBBands.forEach { band ->
                        SlotCard(
                            band = band,
                            isFavourite = uiState.favouriteIds.contains(band.id),
                            onClick = { onBandClick(band.id) },
                        )
                        Spacer(modifier = Modifier.height(spacing.sm))
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotCard(
    band: Band,
    isFavourite: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val timeStr = "${band.startTime.hour}:${band.startTime.minute.toString().padStart(2, '0')}" +
        " – ${band.endTime.hour}:${band.endTime.minute.toString().padStart(2, '0')}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cardCorner))
            .background(NavyLight)
            .clickable(onClick = onClick)
            .padding(spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = band.name,
                style = MaterialTheme.typography.titleSmall,
                color = White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (isFavourite) {
                FaIcon(
                    name = "heart",
                    size = spacing.iconSm,
                    tint = Crimson,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = timeStr,
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
        Text(
            text = Stages.displayName(band.stageCode),
            style = MaterialTheme.typography.labelSmall,
            color = WhiteAlpha60,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
