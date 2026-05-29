package sk.punkacidetom.pd2026.feature.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import sk.punkacidetom.pd2026.core.ui.theme.Crimson
import sk.punkacidetom.pd2026.core.ui.theme.LocalAppSpacing
import sk.punkacidetom.pd2026.core.ui.theme.Navy
import sk.punkacidetom.pd2026.core.ui.theme.NavyLight
import sk.punkacidetom.pd2026.core.ui.theme.White
import sk.punkacidetom.pd2026.core.ui.theme.WhiteAlpha60

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val spacing = LocalAppSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Recreate the Activity after a locale switch so the new language takes effect immediately
    val activity = LocalContext.current as? Activity
    LaunchedEffect(Unit) {
        viewModel.recreateActivity.collect { activity?.recreate() }
    }

    val updateSuccessMsg = stringResource(R.string.settings_update_success)
    val updateErrorMsg = stringResource(R.string.settings_update_error)

    LaunchedEffect(updateState) {
        when (updateState) {
            UpdateState.SUCCESS -> {
                snackbarHostState.showSnackbar(updateSuccessMsg)
                viewModel.resetUpdateState()
            }
            UpdateState.ERROR -> {
                snackbarHostState.showSnackbar(updateErrorMsg)
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Navy,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Navy)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.md),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.displayMedium,
                color = White,
            )
            Spacer(modifier = Modifier.height(spacing.lg))

            // Language
            SectionLabel(stringResource(R.string.settings_language))
            Spacer(modifier = Modifier.height(spacing.sm))
            Row {
                ToggleButton(
                    label = stringResource(R.string.settings_language_sk),
                    selected = uiState.language == "sk",
                    modifier = Modifier.weight(1f),
                ) { viewModel.setLanguage("sk") }
                Spacer(modifier = Modifier.height(0.dp).also { /* horizontal gap */ })
                ToggleButton(
                    label = stringResource(R.string.settings_language_en),
                    selected = uiState.language == "en",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = spacing.sm),
                ) { viewModel.setLanguage("en") }
            }

            Spacer(modifier = Modifier.height(spacing.lg))

            // Font size
            SectionLabel(stringResource(R.string.settings_font_size))
            Spacer(modifier = Modifier.height(spacing.sm))
            Row {
                ToggleButton(
                    label = stringResource(R.string.settings_font_normal),
                    selected = !uiState.isFontLarge,
                    modifier = Modifier.weight(1f),
                ) { viewModel.setFontLarge(false) }
                ToggleButton(
                    label = stringResource(R.string.settings_font_large),
                    selected = uiState.isFontLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = spacing.sm),
                ) { viewModel.setFontLarge(true) }
            }

            Spacer(modifier = Modifier.height(spacing.lg))

            // Update data
            Button(
                onClick = { viewModel.triggerDataUpdate() },
                enabled = updateState != UpdateState.UPDATING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.buttonMinHeight),
                colors = ButtonDefaults.buttonColors(containerColor = Crimson),
            ) {
                Text(
                    text = if (updateState == UpdateState.UPDATING)
                        stringResource(R.string.settings_updating)
                    else
                        stringResource(R.string.settings_update_data),
                    style = MaterialTheme.typography.labelLarge,
                    color = White,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(spacing.xl))

            Text(
                text = stringResource(R.string.settings_credit),
                style = MaterialTheme.typography.bodySmall,
                color = WhiteAlpha60,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = White,
    )
}

@Composable
private fun ToggleButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    Button(
        onClick = onClick,
        modifier = modifier.height(spacing.buttonMinHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Crimson else NavyLight,
            contentColor = if (selected) White else WhiteAlpha60,
        ),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}
