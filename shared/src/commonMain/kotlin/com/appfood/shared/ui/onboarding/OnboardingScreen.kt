package com.appfood.shared.ui.onboarding

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.NiveauActivite
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.Sexe
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.SkipLink

/**
 * Onboarding screen (PROFIL-01) — multi-step profile questionnaire.
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()

    // Handle completion
    when (state) {
        is OnboardingState.Complete -> {
            onOnboardingComplete()
        }
        else -> { /* No-op */ }
    }

    OnboardingContent(
        currentStep = currentStep,
        totalSteps = OnboardingViewModel.TOTAL_STEPS,
        isSaving = state is OnboardingState.Saving,
        errorMessage = (state as? OnboardingState.Error)?.message,
        viewModel = viewModel,
        onContinue = viewModel::onContinue,
        onBack = viewModel::onBack,
        onSkip = viewModel::onSkip,
    )
}

@Composable
private fun OnboardingContent(
    currentStep: Int,
    totalSteps: Int,
    isSaving: Boolean,
    errorMessage: String?,
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Etape $currentStep sur $totalSteps",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error
        if (errorMessage != null) {
            ErrorMessage(message = errorMessage)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Step content (scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            when (currentStep) {
                1 -> Step1BodyMetrics(viewModel)
                2 -> Step2DietType(viewModel)
                3 -> Step3ActivityLevel(viewModel)
                4 -> Step4Exclusions(viewModel)
            }
        }

        // Bottom buttons
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinue,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp),
                )
            } else {
                Text(
                    if (currentStep == totalSteps) Strings.ONBOARDING_FINISH
                    else Strings.ONBOARDING_CONTINUE,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Skip link — centered at bottom
        SkipLink(
            text = Strings.ONBOARDING_SKIP,
            onClick = onSkip,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// --- Step 1: Body metrics ---

@Composable
private fun Step1BodyMetrics(viewModel: OnboardingViewModel) {
    val sexe by viewModel.sexe.collectAsState()
    val ageText by viewModel.ageText.collectAsState()
    val poidsText by viewModel.poidsText.collectAsState()
    val tailleText by viewModel.tailleText.collectAsState()
    val error by viewModel.step1Error.collectAsState()

    Text(
        text = Strings.ONBOARDING_STEP1_TITLE,
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(modifier = Modifier.height(24.dp))

    // Sexe selection
    Text(
        text = Strings.ONBOARDING_SEXE_LABEL,
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SelectableCard(
            label = Strings.ONBOARDING_SEXE_HOMME,
            selected = sexe == Sexe.HOMME,
            onClick = { viewModel.onSexeChanged(Sexe.HOMME) },
            modifier = Modifier.weight(1f),
        )
        SelectableCard(
            label = Strings.ONBOARDING_SEXE_FEMME,
            selected = sexe == Sexe.FEMME,
            onClick = { viewModel.onSexeChanged(Sexe.FEMME) },
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Age
    OutlinedTextField(
        value = ageText,
        onValueChange = viewModel::onAgeChanged,
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

    // Poids and Taille side by side
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = poidsText,
            onValueChange = viewModel::onPoidsChanged,
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
            onValueChange = viewModel::onTailleChanged,
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

    // Validation error
    if (error != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = error!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

// --- Step 2: Diet type ---

@Composable
private fun Step2DietType(viewModel: OnboardingViewModel) {
    val selected by viewModel.regimeAlimentaire.collectAsState()

    Text(
        text = Strings.ONBOARDING_STEP2_TITLE,
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(modifier = Modifier.height(24.dp))

    val options = listOf(
        RegimeAlimentaire.VEGAN to (Strings.ONBOARDING_REGIME_VEGAN to Strings.ONBOARDING_REGIME_VEGAN_DESC),
        RegimeAlimentaire.VEGETARIEN to (Strings.ONBOARDING_REGIME_VEGETARIEN to Strings.ONBOARDING_REGIME_VEGETARIEN_DESC),
        RegimeAlimentaire.FLEXITARIEN to (Strings.ONBOARDING_REGIME_FLEXITARIEN to Strings.ONBOARDING_REGIME_FLEXITARIEN_DESC),
        RegimeAlimentaire.OMNIVORE to (Strings.ONBOARDING_REGIME_OMNIVORE to Strings.ONBOARDING_REGIME_OMNIVORE_DESC),
    )

    options.forEach { (regime, labelAndDesc) ->
        SelectableCardWithDescription(
            label = labelAndDesc.first,
            description = labelAndDesc.second,
            selected = selected == regime,
            onClick = { viewModel.onRegimeChanged(regime) },
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// --- Step 3: Activity level ---

@Composable
private fun Step3ActivityLevel(viewModel: OnboardingViewModel) {
    val selected by viewModel.niveauActivite.collectAsState()

    Text(
        text = Strings.ONBOARDING_STEP3_TITLE,
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(modifier = Modifier.height(24.dp))

    val options = listOf(
        NiveauActivite.SEDENTAIRE to (Strings.ONBOARDING_ACTIVITE_SEDENTAIRE to Strings.ONBOARDING_ACTIVITE_SEDENTAIRE_DESC),
        NiveauActivite.LEGER to (Strings.ONBOARDING_ACTIVITE_LEGER to Strings.ONBOARDING_ACTIVITE_LEGER_DESC),
        NiveauActivite.MODERE to (Strings.ONBOARDING_ACTIVITE_MODERE to Strings.ONBOARDING_ACTIVITE_MODERE_DESC),
        NiveauActivite.ACTIF to (Strings.ONBOARDING_ACTIVITE_ACTIF to Strings.ONBOARDING_ACTIVITE_ACTIF_DESC),
        NiveauActivite.TRES_ACTIF to (Strings.ONBOARDING_ACTIVITE_TRES_ACTIF to Strings.ONBOARDING_ACTIVITE_TRES_ACTIF_DESC),
    )

    options.forEach { (niveau, labelAndDesc) ->
        SelectableCardWithDescription(
            label = labelAndDesc.first,
            description = labelAndDesc.second,
            selected = selected == niveau,
            onClick = { viewModel.onActiviteChanged(niveau) },
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// --- Step 4: Exclusions and allergies ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step4Exclusions(viewModel: OnboardingViewModel) {
    val selectedAllergies by viewModel.selectedAllergies.collectAsState()
    val excludedAliments by viewModel.excludedAliments.collectAsState()

    Text(
        text = Strings.ONBOARDING_STEP4_TITLE,
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = Strings.ONBOARDING_STEP4_SUBTITLE,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(24.dp))

    // Predefined allergies as chips
    Text(
        text = Strings.ONBOARDING_ALLERGIES_LABEL,
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(8.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PredefinedAllergies.LIST.forEach { allergie ->
            FilterChip(
                selected = allergie in selectedAllergies,
                onClick = { viewModel.onAllergieToggled(allergie) },
                label = { Text(allergie) },
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Excluded aliments — simple text list for now
    // Full search via Meilisearch will be in PreferencesAlimentairesScreen
    Text(
        text = Strings.ONBOARDING_EXCLUSIONS_SEARCH_LABEL,
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (excludedAliments.isNotEmpty()) {
        excludedAliments.forEach { aliment ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                    modifier = Modifier.clickable { viewModel.onExcludedAlimentRemoved(aliment) },
                )
            }
        }
    }
}

// --- Reusable composables ---

@Composable
private fun SelectableCard(
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
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun SelectableCardWithDescription(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
