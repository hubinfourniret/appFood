package com.appfood.shared.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToConsentSettings: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    onNavigateToPoids: () -> Unit,
    onNavigateToHydratation: () -> Unit,
    onNavigateToQuotaManagement: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToSocialSettings: () -> Unit,
) {
    SettingsContent(
        onNavigateBack = onNavigateBack,
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToConsentSettings = onNavigateToConsentSettings,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateToTermsOfService = onNavigateToTermsOfService,
        onNavigateToPoids = onNavigateToPoids,
        onNavigateToHydratation = onNavigateToHydratation,
        onNavigateToQuotaManagement = onNavigateToQuotaManagement,
        onNavigateToFaq = onNavigateToFaq,
        onNavigateToSocialSettings = onNavigateToSocialSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToConsentSettings: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    onNavigateToPoids: () -> Unit,
    onNavigateToHydratation: () -> Unit,
    onNavigateToQuotaManagement: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToSocialSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Strings.SETTINGS_TITLE) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = Strings.SETTINGS_BACK)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            // General section
            SettingsSectionTitle(text = Strings.SETTINGS_SECTION_GENERAL)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsLinkItem(label = Strings.SETTINGS_SOCIAL_PROFILE, onClick = onNavigateToSocialSettings)
            HorizontalDivider()
            SettingsLinkItem(label = Strings.SETTINGS_ABOUT, onClick = onNavigateToAbout)
            HorizontalDivider()
            SettingsLinkItem(label = Strings.SETTINGS_FAQ, onClick = onNavigateToFaq)
            HorizontalDivider()
            SettingsLinkItem(label = Strings.SETTINGS_CONSENT, onClick = onNavigateToConsentSettings)

            Spacer(modifier = Modifier.height(24.dp))

            // Health section
            SettingsSectionTitle(text = Strings.SETTINGS_SECTION_HEALTH)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsLinkItem(label = Strings.SETTINGS_POIDS, onClick = onNavigateToPoids)
            HorizontalDivider()
            SettingsLinkItem(label = Strings.SETTINGS_HYDRATATION, onClick = onNavigateToHydratation)
            HorizontalDivider()
            SettingsLinkItem(label = Strings.SETTINGS_QUOTAS, onClick = onNavigateToQuotaManagement)

            Spacer(modifier = Modifier.height(24.dp))

            // Legal section
            SettingsSectionTitle(text = Strings.SETTINGS_SECTION_LEGAL)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsLinkItem(label = Strings.SETTINGS_PRIVACY, onClick = onNavigateToPrivacyPolicy)
            HorizontalDivider()
            SettingsLinkItem(label = Strings.SETTINGS_CGU, onClick = onNavigateToTermsOfService)

            Spacer(modifier = Modifier.height(24.dp))

            // Version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = Strings.SETTINGS_VERSION,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = Strings.ABOUT_APP_VERSION,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun SettingsLinkItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = ">",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
