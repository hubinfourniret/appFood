package com.appfood.shared.ui.profil

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.NiveauActivite
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.Sexe
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton

/**
 * Edit profile screen (PROFIL-02).
 * Same fields as onboarding but pre-filled with existing values.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilScreen(
    viewModel: ProfilViewModel,
    onNavigateBack: () -> Unit = {},
    onSaveSuccess: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle save success notification
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.SCREEN_EDIT_PROFIL) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(Strings.ABOUT_BACK)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (state) {
            is ProfilState.Loading -> {
                LoadingSkeleton(
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is ProfilState.Error -> {
                ErrorMessage(
                    message = (state as ProfilState.Error).message,
                    onRetry = viewModel::loadProfile,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is ProfilState.Loaded -> {
                val sexe by viewModel.sexe.collectAsState()
                val ageText by viewModel.ageText.collectAsState()
                val poidsText by viewModel.poidsText.collectAsState()
                val tailleText by viewModel.tailleText.collectAsState()
                val regime by viewModel.regimeAlimentaire.collectAsState()
                val activite by viewModel.niveauActivite.collectAsState()
                val editError by viewModel.editError.collectAsState()

                EditProfilContent(
                    sexe = sexe,
                    ageText = ageText,
                    poidsText = poidsText,
                    tailleText = tailleText,
                    regime = regime,
                    activite = activite,
                    editError = editError,
                    isSaving = saveState is SaveState.Saving,
                    snackbarHostState = snackbarHostState,
                    onSexeChanged = viewModel::onSexeChanged,
                    onAgeChanged = viewModel::onAgeChanged,
                    onPoidsChanged = viewModel::onPoidsChanged,
                    onTailleChanged = viewModel::onTailleChanged,
                    onRegimeChanged = viewModel::onRegimeChanged,
                    onActiviteChanged = viewModel::onActiviteChanged,
                    onSaveProfile = viewModel::onSaveProfile,
                )
            }
        }
    }
}

@Composable
private fun EditProfilContent(
    sexe: Sexe?,
    ageText: String,
    poidsText: String,
    tailleText: String,
    regime: RegimeAlimentaire?,
    activite: NiveauActivite?,
    editError: String?,
    isSaving: Boolean,
    snackbarHostState: SnackbarHostState,
    onSexeChanged: (Sexe) -> Unit,
    onAgeChanged: (String) -> Unit,
    onPoidsChanged: (String) -> Unit,
    onTailleChanged: (String) -> Unit,
    onRegimeChanged: (RegimeAlimentaire) -> Unit,
    onActiviteChanged: (NiveauActivite) -> Unit,
    onSaveProfile: () -> Unit,
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
            // Section: Body metrics
            Text(
                text = Strings.PROFIL_SECTION_BODY,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Sexe
            Text(
                text = Strings.ONBOARDING_SEXE_LABEL,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SelectableChip(
                    label = Strings.ONBOARDING_SEXE_HOMME,
                    selected = sexe == Sexe.HOMME,
                    onClick = { onSexeChanged(Sexe.HOMME) },
                    modifier = Modifier.weight(1f),
                )
                SelectableChip(
                    label = Strings.ONBOARDING_SEXE_FEMME,
                    selected = sexe == Sexe.FEMME,
                    onClick = { onSexeChanged(Sexe.FEMME) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Age
            OutlinedTextField(
                value = ageText,
                onValueChange = onAgeChanged,
                label = { Text(Strings.ONBOARDING_AGE_LABEL) },
                suffix = { Text(Strings.ONBOARDING_AGE_UNIT) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Poids + Taille
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = poidsText,
                    onValueChange = onPoidsChanged,
                    label = { Text(Strings.ONBOARDING_POIDS_LABEL) },
                    suffix = { Text(Strings.ONBOARDING_POIDS_UNIT) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = tailleText,
                    onValueChange = onTailleChanged,
                    label = { Text(Strings.ONBOARDING_TAILLE_LABEL) },
                    suffix = { Text(Strings.ONBOARDING_TAILLE_UNIT) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Diet
            Text(
                text = Strings.PROFIL_SECTION_DIET,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            RegimeAlimentaire.entries.forEach { r ->
                val label = when (r) {
                    RegimeAlimentaire.VEGAN -> Strings.ONBOARDING_REGIME_VEGAN
                    RegimeAlimentaire.VEGETARIEN -> Strings.ONBOARDING_REGIME_VEGETARIEN
                    RegimeAlimentaire.FLEXITARIEN -> Strings.ONBOARDING_REGIME_FLEXITARIEN
                    RegimeAlimentaire.OMNIVORE -> Strings.ONBOARDING_REGIME_OMNIVORE
                }
                SelectableChip(
                    label = label,
                    selected = regime == r,
                    onClick = { onRegimeChanged(r) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Activity
            Text(
                text = Strings.PROFIL_SECTION_ACTIVITY,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            NiveauActivite.entries.forEach { n ->
                val label = when (n) {
                    NiveauActivite.SEDENTAIRE -> Strings.ONBOARDING_ACTIVITE_SEDENTAIRE
                    NiveauActivite.LEGER -> Strings.ONBOARDING_ACTIVITE_LEGER
                    NiveauActivite.MODERE -> Strings.ONBOARDING_ACTIVITE_MODERE
                    NiveauActivite.ACTIF -> Strings.ONBOARDING_ACTIVITE_ACTIF
                    NiveauActivite.TRES_ACTIF -> Strings.ONBOARDING_ACTIVITE_TRES_ACTIF
                }
                SelectableChip(
                    label = label,
                    selected = activite == n,
                    onClick = { onActiviteChanged(n) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
            }

            // Validation error
            if (editError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = editError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Save button fixed at bottom
        Button(
            onClick = onSaveProfile,
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

    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textAlign = TextAlign.Center,
        )
    }
}
