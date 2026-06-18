package sk.punkacidetom.pd2026.feature.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

@Composable
fun NewsScreen(
    onOpenVolume: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = LocalAppSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy)
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.newsletter_title),
            style = MaterialTheme.typography.displayMedium,
            color = White,
        )
        Spacer(modifier = Modifier.height(spacing.sm))

        if (uiState.volumes.isEmpty()) {
            Text(
                text = stringResource(R.string.newsletter_not_yet),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60,
            )
        } else {
            uiState.volumes.forEach { volume ->
                Button(
                    onClick = { onOpenVolume(volume.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.homeButtonMinHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                ) {
                    Text(
                        text = stringResource(R.string.newsletter_volume, volume.id),
                        style = MaterialTheme.typography.headlineMedium,
                        color = White,
                    )
                }
            }
        }
    }
}
