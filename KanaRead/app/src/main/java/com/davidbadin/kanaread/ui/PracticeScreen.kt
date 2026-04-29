package com.davidbadin.kanaread.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.collectAsState
import com.davidbadin.kanaread.viewmodel.PracticeViewModel
import com.davidbadin.kanaread.ui.theme.SuccessGreen
import com.davidbadin.kanaread.ui.theme.ErrorRed

/**
 * Practice screen — shows a kana word, accepts romaji input,
 * checks the answer, and tracks session stats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    mode: String,
    viewModel: PracticeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Start a fresh session when this screen first appears.
    LaunchedEffect(mode) {
        viewModel.startNewSession(mode)
    }

    var showEndDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-focus the input (and show the keyboard) on each new word.
    LaunchedEffect(state.currentWord?.id, state.isAnswerChecked) {
        if (state.currentWord != null && !state.isAnswerChecked) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else if (state.isAnswerChecked) {
            keyboardController?.hide()
        }
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("End session?") },
            text = { Text("You will lose your current progress.") },
            confirmButton = {
                TextButton(onClick = {
                    showEndDialog = false
                    onBack()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(label = "Words", value = state.sessionWordCount.toString())
                        StatChip(
                            label = "Score",
                            value = "${state.sessionSuccessRate.toInt()}%"
                        )
                        StatChip(
                            label = "Avg time",
                            value = "${"%.1f".format(state.averageTimePerCorrectWord)}s"
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showEndDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(Modifier.size(8.dp))

                    KanaCard(text = state.currentWord?.kana ?: "")

                    OutlinedTextField(
                        value = state.userInput,
                        onValueChange = { viewModel.onInputChange(it) },
                        placeholder = { Text("Type the reading...") },
                        singleLine = true,
                        enabled = !state.isAnswerChecked,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            capitalization = KeyboardCapitalization.None
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.checkAnswer() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    if (!state.isAnswerChecked) {
                        Button(
                            onClick = { viewModel.checkAnswer() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Check",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    } else {
                        FeedbackBlock(
                            isCorrect = state.isCorrect,
                            english = state.currentWord?.english ?: "",
                            correctRomaji = state.currentWord?.romaji ?: "",
                            showRomaji = !state.isCorrect
                        )

                        Button(
                            onClick = { viewModel.loadNextWord() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Next word →",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun KanaCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 60.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeedbackBlock(
    isCorrect: Boolean,
    english: String,
    correctRomaji: String,
    showRomaji: Boolean
) {
    val color: Color = if (isCorrect) SuccessGreen else ErrorRed
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isCorrect) "Correct! ✓" else "Incorrect ✗",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Meaning: $english",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (showRomaji) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Reading: $correctRomaji",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
