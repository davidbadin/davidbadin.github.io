package sk.punkacidetom.pd2026.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import sk.punkacidetom.pd2026.core.model.Band
import sk.punkacidetom.pd2026.core.model.Stages
import sk.punkacidetom.pd2026.core.ui.icons.FaIcon
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.NavyDark
import sk.punkacidetom.pd2026.core.ui.theme.White
import java.time.LocalDateTime
import java.time.LocalTime

data class NowPlayingSlot(
    val band: Band,
    val progress: Float,
)

/**
 * Banner visible only when ≥1 band is currently playing.
 * Self-ticks every minute to auto-appear/disappear as slots change.
 */
@Composable
fun NowPlayingHeader(
    bands: List<Band>,
    onBandClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    // Tick every 60 seconds to refresh which slots are active
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = LocalDateTime.now()
        }
    }

    val nowTime: LocalTime = now.toLocalTime()
    val nowDate = now.toLocalDate()

    val playing = bands.filter { band ->
        val sameStartDate = band.startDate == nowDate || band.endDate == nowDate
        if (!sameStartDate) return@filter false
        val startDt = band.startDate.atTime(band.startTime)
        val endDt = band.endDate.atTime(band.endTime)
        !now.isBefore(startDt) && now.isBefore(endDt)
    }.map { band ->
        val startDt = band.startDate.atTime(band.startTime)
        val endDt = band.endDate.atTime(band.endTime)
        val total = java.time.Duration.between(startDt, endDt).seconds.toFloat()
        val elapsed = java.time.Duration.between(startDt, now).seconds.toFloat()
        NowPlayingSlot(band, if (total > 0) (elapsed / total).coerceIn(0f, 1f) else 0f)
    }

    AnimatedVisibility(
        visible = playing.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark),
        ) {
            playing.forEach { slot ->
                NowPlayingRow(slot = slot, onClick = { onBandClick(slot.band.id) })
            }
        }
    }
}

@Composable
private fun NowPlayingRow(slot: NowPlayingSlot, onClick: () -> Unit) {
    val spacing = sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FaIcon(
                name = "play",
                size = spacing.iconSm,
                tint = Crimson,
                modifier = Modifier.padding(end = spacing.sm),
            )
            Text(
                text = slot.band.name,
                style = MaterialTheme.typography.titleMedium,
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = Stages.displayName(slot.band.stageCode),
                style = MaterialTheme.typography.labelSmall,
                color = Crimson,
            )
        }
        LinearProgressIndicator(
            progress = { slot.progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(2.dp),
            color = Crimson,
            trackColor = Color.White.copy(alpha = 0.15f),
        )
    }
}
