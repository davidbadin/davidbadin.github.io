package com.davidbadin.kanaread.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Brief explanation of the romaji style the app expects.
 *
 * Shown as a dialog from both the main screen and the practice screen.
 */
@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "How to type",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Type the romaji as you would on a Japanese IME — " +
                            "letters that produce the kana you see.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))

                HelpRow("Long vowels are doubled:")
                Example("おはよう = ohayou")
                Example("おねえさん = oneesan")
                Example("こおり = koori")
                Spacer(Modifier.height(8.dp))

                HelpRow("Small つ doubles the next consonant:")
                Example("がっこう = gakkou")
                Example("きっぷ = kippu")
                Example("いっしょ = issho")
                Spacer(Modifier.height(8.dp))

                HelpRow("ん is just \"n\":")
                Example("さん = san")
                Example("にほん = nihon")
                Spacer(Modifier.height(8.dp))

                HelpRow("Some kana have specific spellings:")
                Example("し = shi    ち = chi")
                Example("つ = tsu    ふ = fu")
                Example("じ = ji     ぢ = ji    づ = zu")
                Spacer(Modifier.height(8.dp))

                HelpRow("Katakana long vowel ー uses a doubled vowel:")
                Example("コーヒー = koohii")
                Example("チーズ = chiizu")
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "No diacritics or apostrophes — just type what you see.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun HelpRow(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun Example(text: String) {
    Text(
        text = "  • $text",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}
