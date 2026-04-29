package com.davidbadin.kanaread.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lists the user's personal best average-time-per-correct-word for each
 * mode. A dash is shown when no record has been set yet (less than 10
 * correct answers in any session for that mode).
 */
@Composable
fun BestRecordsDialog(
    bestHiragana: Float?,
    bestKatakana: Float?,
    bestBoth: Float?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Personal best",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Fastest average time per correct word.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(12.dp))

                RecordRow(label = "Hiragana", value = bestHiragana)
                RecordRow(label = "Katakana", value = bestKatakana)
                RecordRow(label = "Both", value = bestBoth)

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "A record requires at least 10 correct words in a session.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun RecordRow(label: String, value: Float?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (value != null) "%.1fs".format(value) else "—",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (value != null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
    }
}
