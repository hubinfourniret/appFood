package com.appfood.shared.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfood.shared.api.response.JournalEntryResponse
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.EmptyJournalState

/**
 * Journal entries screen (JOURNAL-06).
 * Displays today's journal entries with delete button and confirmation dialog.
 */
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onNavigateToAddEntry: () -> Unit,
) {
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    val editState by viewModel.editState.collectAsState()

    // Reset edit state when entering screen
    LaunchedEffect(Unit) {
        viewModel.resetEditState()
    }

    // Delete confirmation dialog (JOURNAL-06)
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelDelete() },
            title = { Text(Strings.JOURNAL_DELETE_CONFIRM_TITLE) },
            text = { Text(Strings.JOURNAL_DELETE_CONFIRM_MESSAGE) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onConfirmDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(Strings.JOURNAL_DELETE_CONFIRM)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelDelete() }) {
                    Text(Strings.JOURNAL_DELETE_CANCEL)
                }
            },
        )
    }

    JournalScreenContent(
        viewModel = viewModel,
        onNavigateToAddEntry = onNavigateToAddEntry,
        onRequestDelete = viewModel::onRequestDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalScreenContent(
    viewModel: JournalViewModel,
    onNavigateToAddEntry: () -> Unit,
    onRequestDelete: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.SCREEN_JOURNAL) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Button(
                onClick = onNavigateToAddEntry,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(Strings.DASHBOARD_ADD_MEAL)
            }

            // Entries will be loaded from the ViewModel's journal entries
            // For now, display a placeholder that encourages adding entries
            EmptyJournalState(onAddEntry = onNavigateToAddEntry)
        }
    }
}

/**
 * Card for a journal entry with a delete button (JOURNAL-06).
 * Reusable component for any screen that displays journal entries.
 */
@Composable
fun JournalEntryCard(
    entry: JournalEntryResponse,
    onRequestDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = entry.nom,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${entry.quantiteGrammes.toInt()}g - ${entry.nutrimentsCalcules.calories.toInt()} ${Strings.JOURNAL_UNIT_KCAL}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TextButton(
                onClick = { onRequestDelete(entry.id) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(Strings.JOURNAL_DELETE_ENTRY)
            }
        }
    }
}
