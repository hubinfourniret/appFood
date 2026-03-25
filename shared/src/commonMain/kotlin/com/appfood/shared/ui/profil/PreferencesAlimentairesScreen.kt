package com.appfood.shared.ui.profil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.onboarding.PredefinedAllergies

/**
 * Preferences alimentaires screen (PROFIL-03).
 * Allows managing food exclusions (with Meilisearch) and allergy checkboxes.
 */
@Composable
fun PreferencesAlimentairesScreen(
    viewModel: ProfilViewModel,
    onSaveSuccess: () -> Unit,
) {
    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (val current = saveState) {
            is SaveState.Success -> {
                snackbarHostState.showSnackbar(current.message)
                viewModel.resetSaveState()
                onSaveSuccess()
            }
            else -> { /* No-op */ }
        }
    }

    val selectedAllergies by viewModel.selectedAllergies.collectAsState()
    val excludedAliments by viewModel.excludedAliments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    PreferencesContent(
        selectedAllergies = selectedAllergies,
        excludedAliments = excludedAliments,
        searchQuery = searchQuery,
        searchResults = searchResults,
        isSaving = saveState is SaveState.Saving,
        snackbarHostState = snackbarHostState,
        onAllergieToggled = viewModel::onAllergieToggled,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onExcludedAlimentAdded = viewModel::onExcludedAlimentAdded,
        onExcludedAlimentRemoved = viewModel::onExcludedAlimentRemoved,
        onSavePreferences = viewModel::onSavePreferences,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferencesContent(
    selectedAllergies: Set<String>,
    excludedAliments: List<String>,
    searchQuery: String,
    searchResults: List<String>,
    isSaving: Boolean,
    snackbarHostState: SnackbarHostState,
    onAllergieToggled: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onExcludedAlimentAdded: (String) -> Unit,
    onExcludedAlimentRemoved: (String) -> Unit,
    onSavePreferences: () -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = Strings.PREFERENCES_TITLE,
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Allergies section
            Text(
                text = Strings.PREFERENCES_ALLERGIES_SECTION,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PredefinedAllergies.LIST.forEach { allergie ->
                    FilterChip(
                        selected = allergie in selectedAllergies,
                        onClick = { onAllergieToggled(allergie) },
                        label = { Text(allergie) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Excluded aliments section
            Text(
                text = Strings.PREFERENCES_EXCLUDED_SECTION,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text(Strings.PREFERENCES_SEARCH_PLACEHOLDER) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Search results
            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                    ) {
                        searchResults.forEach { result ->
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onExcludedAlimentAdded(result) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current exclusions list
            if (excludedAliments.isEmpty()) {
                Text(
                    text = Strings.PREFERENCES_NO_EXCLUSIONS,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                excludedAliments.forEach { aliment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = aliment,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = Strings.PREFERENCES_REMOVE,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clickable { onExcludedAlimentRemoved(aliment) }
                                .padding(horizontal = 8.dp),
                        )
                    }
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Save button
        Button(
            onClick = onSavePreferences,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(48.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp),
                )
            } else {
                Text(Strings.PROFIL_SAVE_BUTTON)
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
