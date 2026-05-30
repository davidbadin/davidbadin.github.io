package sk.punkacidetom.pd2026.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.Stages
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.LocalFontScaleMultiplier
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60
import java.time.Duration
import java.time.LocalDateTime
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

    // Scale card height with font size — 50% taller at all scales so text fits
    val fontScale = LocalFontScaleMultiplier.current
    val minuteHeightDp = 2f * 1.5f * fontScale   // dp per timeline-minute

    // Ticking clock for the "LIVE" indicator — updates every 60 seconds
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = LocalDateTime.now()
        }
    }

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

        // Day tab selector — each button shares the full row width equally
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
                            .weight(1f)
                            .clip(RoundedCornerShape(spacing.cardCorner))
                            .background(if (selected) Crimson else NavyLight),
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) White else WhiteAlpha60,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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

        // Two-column proportional timetable
        val allBands = uiState.stageABands + uiState.stageBBands
        if (allBands.isEmpty()) {
            Text(
                text = stringResource(R.string.timetable_no_slots),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60,
                modifier = Modifier.padding(spacing.md),
            )
        } else {
            // Compute timeline bounds with LocalDateTime to handle midnight crossover
            val dayStartDt = allBands.minOf { LocalDateTime.of(it.startDate, it.startTime) }
            val dayEndDt = allBands.maxOf { LocalDateTime.of(it.endDate, it.endTime) }
            val totalMinutes = Duration.between(dayStartDt, dayEndDt).toMinutes()
            val totalTimelineHeight = (totalMinutes * minuteHeightDp).dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                ProportionalStageColumn(
                    bands = uiState.stageABands,
                    dayStartDt = dayStartDt,
                    totalTimelineHeight = totalTimelineHeight,
                    minuteHeightDp = minuteHeightDp,
                    favouriteIds = uiState.favouriteIds,
                    now = now,
                    onBandClick = onBandClick,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(spacing.sm))

                ProportionalStageColumn(
                    bands = uiState.stageBBands,
                    dayStartDt = dayStartDt,
                    totalTimelineHeight = totalTimelineHeight,
                    minuteHeightDp = minuteHeightDp,
                    favouriteIds = uiState.favouriteIds,
                    now = now,
                    onBandClick = onBandClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ProportionalStageColumn(
    bands: List<Band>,
    dayStartDt: LocalDateTime,
    totalTimelineHeight: Dp,
    minuteHeightDp: Float,
    favouriteIds: Set<Int>,
    now: LocalDateTime,
    onBandClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Remove overlapping bands within this column (keep earlier ones, skip later ones)
    val dedupedBands: List<Band> = buildList {
        var lastEndDt = LocalDateTime.MIN
        for (band in bands.sortedWith(compareBy({ it.startDate }, { it.startTime }))) {
            val bandStartDt = LocalDateTime.of(band.startDate, band.startTime)
            if (bandStartDt >= lastEndDt) {
                add(band)
                lastEndDt = LocalDateTime.of(band.endDate, band.endTime)
            }
            // else: skip — overlapping entry in same column
        }
    }

    Box(modifier = modifier.height(totalTimelineHeight)) {
        dedupedBands.forEach { band ->
            val bandStartDt = LocalDateTime.of(band.startDate, band.startTime)
            val bandEndDt = LocalDateTime.of(band.endDate, band.endTime)

            val offsetMinutes = Duration.between(dayStartDt, bandStartDt).toMinutes()
            val durationMinutes = Duration.between(bandStartDt, bandEndDt).toMinutes()
                .coerceAtLeast(1L)

            val offsetDp = (offsetMinutes * minuteHeightDp).dp
            val heightDp = (durationMinutes * minuteHeightDp).dp

            val isPlaying = !now.isBefore(bandStartDt) && now.isBefore(bandEndDt)

            SlotCard(
                band = band,
                isFavourite = favouriteIds.contains(band.id),
                isPlaying = isPlaying,
                onClick = { onBandClick(band.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .absoluteOffset(y = offsetDp)
                    .height(heightDp),
            )
        }
    }
}

@Composable
private fun SlotCard(
    band: Band,
    isFavourite: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val timeStr = "${band.startTime.hour}:${band.startTime.minute.toString().padStart(2, '0')}" +
        " – ${band.endTime.hour}:${band.endTime.minute.toString().padStart(2, '0')}"
    val cornerShape = RoundedCornerShape(spacing.cardCorner)

    Column(
        modifier = modifier
            .clip(cornerShape)
            .background(if (isPlaying) Crimson.copy(alpha = 0.85f) else NavyLight)
            .then(
                if (isPlaying) Modifier.border(2.dp, Crimson, cornerShape)
                else Modifier
            )
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
            if (isPlaying) {
                Text(
                    text = "▶ LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = White,
                    modifier = Modifier.padding(start = 4.dp),
                )
            } else if (isFavourite) {
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
            color = if (isPlaying) White.copy(alpha = 0.85f) else Crimson,
        )
        if (band.genre.isNotBlank()) {
            Text(
                text = band.genre,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPlaying) White.copy(alpha = 0.7f) else WhiteAlpha60,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // Stage name intentionally omitted — shown in the column header above
    }
}
