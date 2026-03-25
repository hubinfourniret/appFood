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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    onNavigateToEditProfil: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    ProfilContent(
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
