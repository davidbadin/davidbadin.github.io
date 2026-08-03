package sk.punkacidetom.pd2026.feature.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import sk.punkacidetom.pd2026.core.model.Band
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import sk.punkacidetom.pd2026.core.ui.components.FestivalScreenHeader
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

private const val GLOW_HEIGHT_MINUTES             = 15
private const val GLOW_START_ALPHA                = 0.5f
private const val INACTIVE_DAY_BUTTON_ALPHA       = 0.5f
private const val SCHEDULE_DAYBUTTON_TOP_PADDING_DP      = 16
private const val SCHEDULE_STAGE_LABEL_TOP_PADDING_DP    = 4
private const val SCHEDULE_STAGE_LABEL_BOTTOM_PADDING_DP = 4
private const val SWIPE_DAY_THRESHOLD_DP          = 50

@Composable
fun TimetableScreen(
    onBandClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimetableViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDayIndex by viewModel.selectedDayIndex.collectAsState()  // direct — bypasses combine pipeline
    val spacing = LocalAppSpacing.current
    val density = LocalDensity.current

    val fontScale = LocalFontScaleMultiplier.current
    val minuteHeightDp = 2f * 1.5f * fontScale

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = LocalDateTime.now()
        }
    }

    // Hoisted scroll state — retains pixel offset naturally when day content changes;
    // new day starts at the same position as the departing day (desired behaviour).
    val scrollState = rememberScrollState()

    // Swipe to change day
    val swipeThresholdPx = with(density) { SWIPE_DAY_THRESHOLD_DP.dp.toPx() }
    var swipeAccumulator by remember { mutableFloatStateOf(0f) }

    // Measure total static header height for the transparent spacer
    var staticHeaderHeightPx by remember { mutableIntStateOf(0) }
    val staticHeaderHeightDp = with(density) { staticHeaderHeightPx.toDp() }

    Box(modifier = modifier.fillMaxSize().background(Navy)) {

        // Layer 1: scrollable timetable — declared first so it renders at the bottom
        // Swipe gesture lives here so day buttons in the static header are unaffected
        val allBands = uiState.stageABands + uiState.stageBBands
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                swipeAccumulator < -swipeThresholdPx -> viewModel.selectNextDay()
                                swipeAccumulator >  swipeThresholdPx -> viewModel.selectPreviousDay()
                            }
                            swipeAccumulator = 0f
                        },
                        onDragCancel = { swipeAccumulator = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            // change.consume() intentionally omitted — consuming events here
                            // prevents Button click handlers in the static header from firing
                            swipeAccumulator += dragAmount
                        },
                    )
                },
        ) {
            Spacer(modifier = Modifier.height(staticHeaderHeightDp))

            if (allBands.isEmpty()) {
                Text(
                    text = stringResource(R.string.timetable_no_slots),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteAlpha60,
                    modifier = Modifier.padding(spacing.md),
                )
            } else {
                val dayStartDt = allBands.minOf { LocalDateTime.of(it.startDate, it.startTime) }
                val dayEndDt   = allBands.maxOf { LocalDateTime.of(it.endDate, it.endTime) }
                val totalMinutes = Duration.between(dayStartDt, dayEndDt).toMinutes()
                val totalTimelineHeight = (totalMinutes * minuteHeightDp).dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        onToggleFavourite = viewModel::toggleFavourite,
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
                        onToggleFavourite = viewModel::toggleFavourite,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Layer 2: Navy overlay — hides band blocks that have scrolled up into the header area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(staticHeaderHeightDp)
                .background(Navy),
        )

        // Layer 3: static header column (FestivalScreenHeader + day tabs + stage images)
        // zIndex(1f) ensures this is drawn on top of the Navy overlay and scrollable band blocks
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .onSizeChanged { staticHeaderHeightPx = it.height },
        ) {
            FestivalScreenHeader(title = stringResource(R.string.timetable_title))

            // Day tab selector
            if (uiState.days.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top   = SCHEDULE_DAYBUTTON_TOP_PADDING_DP.dp,
                            start = spacing.md,
                            end   = spacing.md,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    uiState.days.forEachIndexed { index, day ->
                        key(index) {
                            val dayName = day.date.dayOfWeek
                                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                                .replaceFirstChar { it.uppercase() }
                            val selected = index == selectedDayIndex  // direct StateFlow — instant update
                            Button(
                                onClick = { viewModel.selectDay(index) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) Crimson else NavyLight,
                                    contentColor   = if (selected) White   else WhiteAlpha60,
                                ),
                                shape = RoundedCornerShape(spacing.cardCorner),
                                contentPadding = PaddingValues(horizontal = spacing.sm, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (selected) 1f else INACTIVE_DAY_BUTTON_ALPHA),
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // Stage header images
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top    = SCHEDULE_STAGE_LABEL_TOP_PADDING_DP.dp,
                        bottom = SCHEDULE_STAGE_LABEL_BOTTOM_PADDING_DP.dp,
                        start  = spacing.md,
                        end    = spacing.md,
                    ),
            ) {
                AsyncImage(
                    model = "file:///android_asset/stage_A.png",
                    contentDescription = "Stage A",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(spacing.sm))
                AsyncImage(
                    model = "file:///android_asset/stage_B.png",
                    contentDescription = "Stage B",
                    contentScale = ContentScale.FillWidth,
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
    onToggleFavourite: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dedupedBands: List<Band> = buildList {
        var lastEndDt = LocalDateTime.MIN
        for (band in bands.sortedWith(compareBy({ it.startDate }, { it.startTime }))) {
            val bandStartDt = LocalDateTime.of(band.startDate, band.startTime)
            if (bandStartDt >= lastEndDt) {
                add(band)
                lastEndDt = LocalDateTime.of(band.endDate, band.endTime)
            }
        }
    }

    Box(modifier = modifier.height(totalTimelineHeight)) {
        dedupedBands.forEach { band ->
            val bandStartDt = LocalDateTime.of(band.startDate, band.startTime)
            val bandEndDt   = LocalDateTime.of(band.endDate,   band.endTime)

            val offsetMinutes   = Duration.between(dayStartDt, bandStartDt).toMinutes()
            val durationMinutes = Duration.between(bandStartDt, bandEndDt).toMinutes().coerceAtLeast(1L)

            val offsetDp     = (offsetMinutes   * minuteHeightDp).dp
            val heightDp     = (durationMinutes * minuteHeightDp).dp
            val glowHeightDp = (GLOW_HEIGHT_MINUTES * minuteHeightDp).dp

            val isPlaying = !now.isBefore(bandStartDt) && now.isBefore(bandEndDt)

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .absoluteOffset(y = offsetDp - glowHeightDp)
                        .height(glowHeightDp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Crimson.copy(alpha = GLOW_START_ALPHA)),
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .absoluteOffset(y = offsetDp + heightDp)
                        .height(glowHeightDp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Crimson.copy(alpha = GLOW_START_ALPHA), Color.Transparent),
                            )
                        )
                )
            }

            SlotCard(
                band = band,
                isFavourite  = favouriteIds.contains(band.id),
                isPlaying    = isPlaying,
                onClick      = { onBandClick(band.id) },
                onToggleFavourite = { onToggleFavourite(band.id) },
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
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val timeStr = "${band.startTime.hour}:${band.startTime.minute.toString().padStart(2, '0')}" +
        " – ${band.endTime.hour}:${band.endTime.minute.toString().padStart(2, '0')}"

    val containerShape = RectangleShape
    val containerColor = when {
        isPlaying   -> Crimson.copy(alpha = 0.85f)
        isFavourite -> Color(0xFFFFAFB0)
        else        -> White
    }
    val nameColor  = if (isPlaying) White else Navy
    val timeColor  = if (isPlaying) White.copy(alpha = 0.85f) else Crimson
    val genreColor = if (isPlaying) White.copy(alpha = 0.7f) else Navy.copy(alpha = 0.65f)

    Column(
        modifier = modifier
            .clip(containerShape)
            .background(containerColor)
            .then(
                if (isPlaying) Modifier.border(2.dp, Crimson, containerShape)
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
                color = nameColor,
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
            }
            Icon(
                imageVector = if (isFavourite) Icons.Filled.Favorite
                              else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = when {
                    isFavourite -> White
                    isPlaying   -> White.copy(alpha = 0.35f)
                    else        -> Navy.copy(alpha = 0.45f)
                },
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(spacing.iconSm)
                    .clickable(onClick = onToggleFavourite),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = timeColor, fontWeight = FontWeight.Bold)
        if (band.genre.isNotBlank()) {
            Text(
                text = band.genre,
                style = MaterialTheme.typography.labelSmall,
                color = genreColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
