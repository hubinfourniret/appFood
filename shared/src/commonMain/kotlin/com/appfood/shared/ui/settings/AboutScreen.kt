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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

/**
 * About screen (SUPPORT-01).
 * Displays app info, support contact, legal links, and legal mentions.
 * Accessible from the Settings screen.
 */
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCgu: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToFaq: () -> Unit,
) {
    AboutContent(
        onNavigateBack = onNavigateBack,
        onNavigateToCgu = onNavigateToCgu,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateToFaq = onNavigateToFaq,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutContent(
    onNavigateBack: () -> Unit,
    onNavigateToCgu: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToFaq: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = Strings.ABOUT_TITLE)
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = Strings.ABOUT_BACK)
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // App name and description
            Text(
                text = Strings.APP_NAME,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.ABOUT_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Version
            AboutInfoRow(
                label = Strings.ABOUT_APP_VERSION_LABEL,
                value = Strings.ABOUT_APP_VERSION,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contact section
            SectionTitle(text = Strings.ABOUT_CONTACT_TITLE)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            uriHandler.openUri("mailto:${Strings.ABOUT_SUPPORT_EMAIL}")
                        }
                        .padding(16.dp),
                ) {
                    Text(
                        text = Strings.ABOUT_SUPPORT_EMAIL_LABEL,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Strings.ABOUT_SUPPORT_EMAIL,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FAQ link (SUPPORT-02)
            SectionTitle(text = Strings.FAQ_TITLE)
            Spacer(modifier = Modifier.height(8.dp))
            AboutLinkItem(
                label = Strings.FAQ_TITLE,
                onClick = onNavigateToFaq,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Legal links section
            SectionTitle(text = Strings.ABOUT_LEGAL_TITLE)
            Spacer(modifier = Modifier.height(8.dp))

            AboutLinkItem(
                label = Strings.ABOUT_CGU,
                onClick = onNavigateToCgu,
            )
            HorizontalDivider()
            AboutLinkItem(
                label = Strings.ABOUT_PRIVACY_POLICY,
                onClick = onNavigateToPrivacyPolicy,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Legal mentions section (App Store / Google Play requirement)
            SectionTitle(text = Strings.ABOUT_LEGAL_MENTIONS_TITLE)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AboutInfoRow(
                        label = Strings.ABOUT_EDITOR_LABEL,
                        value = Strings.ABOUT_EDITOR_VALUE,
                    )
                    HorizontalDivider()
                    AboutInfoRow(
                        label = Strings.ABOUT_HOST_LABEL,
                        value = Strings.ABOUT_HOST_VALUE,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}

@Composable
private fun AboutInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AboutLinkItem(
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
