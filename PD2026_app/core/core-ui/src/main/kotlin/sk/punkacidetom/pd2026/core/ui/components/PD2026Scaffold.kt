package sk.punkacidetom.pd2026.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import sk.punkacidetom.pd2026.core.model.Band

/**
 * App-wide scaffold wrapper that injects the NowPlayingHeader above the page content.
 * The status-bar inset is applied here so the header is never hidden behind the system bar.
 * The bottom bar is provided by the caller (it's navigation-aware).
 */
@Composable
fun PD2026Scaffold(
    bands: List<Band> = emptyList(),
    bottomBar: @Composable () -> Unit = {},
    onNowPlayingBandClick: (Int) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        bottomBar = bottomBar,
        contentWindowInsets = WindowInsets(0), // we handle insets manually below
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            NowPlayingHeader(
                bands = bands,
                onBandClick = onNowPlayingBandClick,
            )
            Box(modifier = Modifier.weight(1f)) {
                content(innerPadding)
            }
        }
    }
}
