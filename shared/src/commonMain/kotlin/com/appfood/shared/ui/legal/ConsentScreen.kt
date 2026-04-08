package com.appfood.shared.ui.legal

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

/**
 * First-launch consent screen (LEGAL-03).
 * Displayed before any data collection occurs.
 * Provides granular toggles: analytics, advertising, service improvement.
 * No tracking is activated without explicit consent (RGPD / ePrivacy compliant).
 */
@Composable
fun ConsentScreen(
    viewModel: ConsentViewModel,
    onConsentConfirmed: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is ConsentState.Confirmed) {
            onConsentConfirmed()
        }
    }

    ConsentContent(
        state = state,
        analyticsEnabled = viewModel.analyticsEnabled.collectAsState().value,
        advertisingEnabled = viewModel.advertisingEnabled.collectAsState().value,
        improvementEnabled = viewModel.improvementEnabled.collectAsState().value,
        onAnalyticsToggled = viewModel::onAnalyticsToggled,
        onAdvertisingToggled = viewModel::onAdvertisingToggled,
        onImprovementToggled = viewModel::onImprovementToggled,
        onAcceptAll = viewModel::onAcceptAll,
        onRefuseAll = viewModel::onRefuseAll,
        onConfirm = viewModel::onConfirmChoices,
    )
}

/**
 * Pure content composable — testable and previewable.
 */
@Composable
private fun ConsentContent(
    state: ConsentState,
    analyticsEnabled: Boolean,
    advertisingEnabled: Boolean,
    improvementEnabled: Boolean,
    onAnalyticsToggled: (Boolean) -> Unit,
    onAdvertisingToggled: (Boolean) -> Unit,
    onImprovementToggled: (Boolean) -> Unit,
    onAcceptAll: () -> Unit,
    onRefuseAll: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Strings.CONSENT_TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = Strings.CONSENT_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Granular consent toggles
        ConsentToggleCard(
            label = Strings.CONSENT_ANALYTICS_LABEL,
            description = Strings.CONSENT_ANALYTICS_DESC,
            checked = analyticsEnabled,
            onCheckedChange = onAnalyticsToggled,
            enabled = state !is ConsentState.Saving,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConsentToggleCard(
            label = Strings.CONSENT_ADVERTISING_LABEL,
            description = Strings.CONSENT_ADVERTISING_DESC,
            checked = advertisingEnabled,
            onCheckedChange = onAdvertisingToggled,
            enabled = state !is ConsentState.Saving,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConsentToggleCard(
            label = Strings.CONSENT_IMPROVEMENT_LABEL,
            description = Strings.CONSENT_IMPROVEMENT_DESC,
            checked = improvementEnabled,
            onCheckedChange = onImprovementToggled,
            enabled = state !is ConsentState.Saving,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bulk action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onRefuseAll,
                modifier = Modifier.weight(1f),
                enabled = state !is ConsentState.Saving,
            ) {
                Text(text = Strings.CONSENT_REFUSE_ALL)
            }

            OutlinedButton(
                onClick = onAcceptAll,
                modifier = Modifier.weight(1f),
                enabled = state !is ConsentState.Saving,
            ) {
                Text(text = Strings.CONSENT_ACCEPT_ALL)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm button
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is ConsentState.Saving,
        ) {
            if (state is ConsentState.Saving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(text = Strings.CONSENT_CONFIRM)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy notice
        Text(
            text = Strings.CONSENT_PRIVACY_NOTICE,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state is ConsentState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (state as ConsentState.Error).message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Individual consent toggle card with label, description, and switch.
 */
@Composable
private fun ConsentToggleCard(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    }
}

/**
 * Consent settings screen (LEGAL-03) — accessible from Settings.
 * Allows modification of consent choices at any time.
 */
@Composable
fun ConsentSettingsScreen(
    viewModel: ConsentViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExistingConsents()
    }

    LaunchedEffect(state) {
        if (state is ConsentState.Confirmed) {
            onNavigateBack()
        }
    }

    ConsentSettingsContent(
        state = state,
        analyticsEnabled = viewModel.analyticsEnabled.collectAsState().value,
        advertisingEnabled = viewModel.advertisingEnabled.collectAsState().value,
        improvementEnabled = viewModel.improvementEnabled.collectAsState().value,
        onAnalyticsToggled = viewModel::onAnalyticsToggled,
        onAdvertisingToggled = viewModel::onAdvertisingToggled,
        onImprovementToggled = viewModel::onImprovementToggled,
        onAcceptAll = viewModel::onAcceptAll,
        onRefuseAll = viewModel::onRefuseAll,
        onConfirm = viewModel::onConfirmChoices,
        onNavigateBack = onNavigateBack,
    )
}

/**
 * Pure content composable for consent settings — testable and previewable.
 */
@Composable
private fun ConsentSettingsContent(
    state: ConsentState,
    analyticsEnabled: Boolean,
    advertisingEnabled: Boolean,
    improvementEnabled: Boolean,
    onAnalyticsToggled: (Boolean) -> Unit,
    onAdvertisingToggled: (Boolean) -> Unit,
    onImprovementToggled: (Boolean) -> Unit,
    onAcceptAll: () -> Unit,
    onRefuseAll: () -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            TextButton(onClick = onNavigateBack) {
                Text(text = Strings.ABOUT_BACK)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = Strings.CONSENT_SETTINGS_TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = Strings.CONSENT_SETTINGS_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Granular consent toggles
        ConsentToggleCard(
            label = Strings.CONSENT_ANALYTICS_LABEL,
            description = Strings.CONSENT_ANALYTICS_DESC,
            checked = analyticsEnabled,
            onCheckedChange = onAnalyticsToggled,
            enabled = state !is ConsentState.Saving,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConsentToggleCard(
            label = Strings.CONSENT_ADVERTISING_LABEL,
            description = Strings.CONSENT_ADVERTISING_DESC,
            checked = advertisingEnabled,
            onCheckedChange = onAdvertisingToggled,
            enabled = state !is ConsentState.Saving,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConsentToggleCard(
            label = Strings.CONSENT_IMPROVEMENT_LABEL,
            description = Strings.CONSENT_IMPROVEMENT_DESC,
            checked = improvementEnabled,
            onCheckedChange = onImprovementToggled,
            enabled = state !is ConsentState.Saving,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bulk action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onRefuseAll,
                modifier = Modifier.weight(1f),
                enabled = state !is ConsentState.Saving,
            ) {
                Text(text = Strings.CONSENT_REFUSE_ALL)
            }

            OutlinedButton(
                onClick = onAcceptAll,
                modifier = Modifier.weight(1f),
                enabled = state !is ConsentState.Saving,
            ) {
                Text(text = Strings.CONSENT_ACCEPT_ALL)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm button
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is ConsentState.Saving,
        ) {
            if (state is ConsentState.Saving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(text = Strings.CONSENT_CONFIRM)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy notice
        Text(
            text = Strings.CONSENT_PRIVACY_NOTICE,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state is ConsentState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (state as ConsentState.Error).message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
