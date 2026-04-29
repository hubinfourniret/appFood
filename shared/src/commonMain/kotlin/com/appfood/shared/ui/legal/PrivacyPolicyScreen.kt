package com.appfood.shared.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings

/**
 * Privacy policy screen (LEGAL-01).
 * Scrollable screen displaying the GDPR-compliant privacy policy placeholder.
 * Accessible from settings and registration flow.
 * Final content to be provided by a legal professional.
 */
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
) {
    PrivacyPolicyContent(
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyPolicyContent(
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.SCREEN_PRIVACY_POLICY) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(Strings.ICON_BACK)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Version date
            Text(
                text = Strings.LEGAL_PRIVACY_POLICY_VERSION,
                style = MaterialTheme.typography.labelMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Introduction
            Text(
                text = Strings.LEGAL_PRIVACY_POLICY_INTRO,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Section 1
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_1_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_1_BODY)

            // Section 2
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_2_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_2_BODY)

            // Section 3
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_3_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_3_BODY)

            // Section 4
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_4_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_4_BODY)

            // Section 5
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_5_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_5_BODY)

            // Section 6
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_6_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_6_BODY)

            // Section 7
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_7_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_7_BODY)

            // Section 8
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_8_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_8_BODY)

            // Section 9
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_SECTION_9_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_SECTION_9_BODY)

            Spacer(modifier = Modifier.height(16.dp))

            // Modification history
            LegalSectionTitle(Strings.LEGAL_PRIVACY_POLICY_HISTORY_TITLE)
            LegalSectionBody(Strings.LEGAL_PRIVACY_POLICY_HISTORY_BODY)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun LegalSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
internal fun LegalSectionBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
