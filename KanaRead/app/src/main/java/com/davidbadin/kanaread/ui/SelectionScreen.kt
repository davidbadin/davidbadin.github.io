package com.davidbadin.kanaread.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbadin.kanaread.data.BestRecordsRepository
import com.davidbadin.kanaread.data.ThemeMode

/**
 * First screen — picks a practice mode.
 *
 * Top bar exposes:
 *  - the theme cycle button (System / Light / Dark)
 *  - the Best Records dialog
 *  - the Help dialog
 *
 * onSelectMode receives "hiragana", "katakana", or "both".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    onSelectMode: (String) -> Unit,
    bestRecords: BestRecordsRepository,
    themeMode: ThemeMode,
    onCycleThemeMode: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }
    var showRecords by remember { mutableStateOf(false) }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }
    if (showRecords) {
        BestRecordsDialog(
            bestHiragana = bestRecords.getBest("hiragana"),
            bestKatakana = bestRecords.getBest("katakana"),
            bestBoth = bestRecords.getBest("both"),
            onDismiss = { showRecords = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onCycleThemeMode) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                ThemeMode.DARK -> Icons.Filled.DarkMode
                            },
                            contentDescription = when (themeMode) {
                                ThemeMode.SYSTEM -> "Theme: follow system"
                                ThemeMode.LIGHT -> "Theme: light"
                                ThemeMode.DARK -> "Theme: dark"
                            },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showRecords = true }) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Best records",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showHelp = true }) {
                        Icon(
                            imageVector = Icons.Filled.HelpOutline,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Kana Practice",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Choose what to practice",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ModeCard(
                title = "Hiragana",
                example = "あ い う",
                onClick = { onSelectMode("hiragana") }
            )

            ModeCard(
                title = "Katakana",
                example = "ア イ ウ",
                onClick = { onSelectMode("katakana") }
            )

            ModeCard(
                title = "Both",
                example = "あ ア  い イ  う ウ",
                onClick = { onSelectMode("both") }
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    example: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = example,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
