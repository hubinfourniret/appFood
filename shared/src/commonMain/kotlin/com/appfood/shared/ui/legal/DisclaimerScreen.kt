package com.appfood.shared.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appfood.shared.ui.Strings

/**
 * Disclaimer screen displayed at first use after registration (UX-05).
 * The user must accept to continue using the app.
 * Also accessible from the "A propos" / legal notices section.
 */
@Composable
fun DisclaimerScreen(
    viewModel: DisclaimerViewModel,
    onDisclaimerAccepted: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is DisclaimerState.Accepted) {
            onDisclaimerAccepted()
        }
    }

    DisclaimerContent(
        state = state,
        onAccept = viewModel::onAcceptDisclaimer,
    )
}

/**
 * Pure composable content for the disclaimer — testable and previewable.
 */
@Composable
private fun DisclaimerContent(
    state: DisclaimerState,
    onAccept: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "\u2139\uFE0F",
            fontSize = 48.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = Strings.DISCLAIMER_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(
                text = Strings.DISCLAIMER_TEXT,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (state is DisclaimerState.Error) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onAccept,
            enabled = state !is DisclaimerState.Saving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state is DisclaimerState.Saving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp),
                )
            } else {
                Text(text = Strings.DISCLAIMER_ACCEPT_BUTTON)
            }
        }
    }
}
