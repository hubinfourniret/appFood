package com.appfood.shared.ui.profil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.auth.AuthViewModel

/**
 * Profile main screen (ProfilScreen).
 * Displays profile summary with navigation to edit, preferences, settings.
 * Includes account deletion (PROFIL-04).
 */
@Composable
fun ProfilScreen(
    authViewModel: AuthViewModel,
    profilViewModel: ProfilViewModel,
    onNavigateToEditProfil: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    val exportState by profilViewModel.exportState.collectAsState()

    ProfilContent(
        exportState = exportState,
        onExportData = { profilViewModel.onExportData() },
        onResetExportState = { profilViewModel.resetExportState() },
        onNavigateToEditProfil = onNavigateToEditProfil,
        onNavigateToPreferences = onNavigateToPreferences,
        onNavigateToSettings = onNavigateToSettings,
        onLogout = {
            authViewModel.onLogout()
            onLogout()
        },
        onDeleteAccount = {
            authViewModel.onDeleteAccount()
            onAccountDeleted()
        },
    )
}

@Composable
private fun ProfilContent(
    exportState: ExportState,
    onExportData: () -> Unit,
    onResetExportState: () -> Unit,
    onNavigateToEditProfil: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmedOnce by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        // Title
        Text(
            text = Strings.SCREEN_PROFIL,
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Menu items
        ProfilMenuItem(
            label = Strings.PROFIL_EDIT,
            onClick = onNavigateToEditProfil,
        )
        HorizontalDivider()
        ProfilMenuItem(
            label = Strings.PROFIL_PREFERENCES,
            onClick = onNavigateToPreferences,
        )
        HorizontalDivider()
        ProfilMenuItem(
            label = Strings.PROFIL_SETTINGS,
            onClick = onNavigateToSettings,
        )
        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        // Export data button
        OutlinedButton(
            onClick = onExportData,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = exportState !is ExportState.Loading,
        ) {
            if (exportState is ExportState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(Strings.PROFIL_EXPORT_LOADING)
            } else {
                Text(Strings.PROFIL_EXPORT_BUTTON)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(Strings.PROFIL_LOGOUT)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete account button (PROFIL-04)
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text(Strings.DELETE_ACCOUNT_BUTTON)
        }
    }

    // Export success dialog
    if (exportState is ExportState.Success) {
        val exportData = exportState.data
        AlertDialog(
            onDismissRequest = onResetExportState,
            title = {
                Text(Strings.PROFIL_EXPORT_SUCCESS_TITLE)
            },
            text = {
                Column {
                    Text(Strings.PROFIL_EXPORT_SUCCESS_MESSAGE)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Strings.PROFIL_EXPORT_SUMMARY_JOURNAL + exportData.journalEntries.size,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = Strings.PROFIL_EXPORT_SUMMARY_QUOTAS + exportData.quotas.size,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = Strings.PROFIL_EXPORT_SUMMARY_POIDS + exportData.poidsHistory.size,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = Strings.PROFIL_EXPORT_SUMMARY_HYDRATATION + exportData.hydratation.size,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Strings.PROFIL_EXPORT_SUMMARY_DATE + exportData.exportedAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onResetExportState) {
                    Text(Strings.PROFIL_EXPORT_CLOSE)
                }
            },
        )
    }

    // Export error dialog
    if (exportState is ExportState.Error) {
        AlertDialog(
            onDismissRequest = onResetExportState,
            title = {
                Text(Strings.PROFIL_EXPORT_ERROR)
            },
            text = {
                Text(exportState.message)
            },
            confirmButton = {
                TextButton(onClick = onResetExportState) {
                    Text(Strings.PROFIL_EXPORT_CLOSE)
                }
            },
        )
    }

    // Delete account dialog with double confirmation (PROFIL-04)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deleteConfirmedOnce = false
            },
            title = {
                Text(Strings.DELETE_ACCOUNT_DIALOG_TITLE)
            },
            text = {
                Column {
                    Text(Strings.DELETE_ACCOUNT_DIALOG_MESSAGE)
                    if (deleteConfirmedOnce) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Strings.DELETE_ACCOUNT_CONFIRM_AGAIN,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                if (deleteConfirmedOnce) {
                    // Second confirmation — actually delete
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            deleteConfirmedOnce = false
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(Strings.DELETE_ACCOUNT_CONFIRM)
                    }
                } else {
                    // First confirmation
                    TextButton(
                        onClick = { deleteConfirmedOnce = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(Strings.DELETE_ACCOUNT_CONFIRM)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteConfirmedOnce = false
                    },
                ) {
                    Text(Strings.DELETE_ACCOUNT_CANCEL)
                }
            },
        )
    }
}

@Composable
private fun ProfilMenuItem(
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
