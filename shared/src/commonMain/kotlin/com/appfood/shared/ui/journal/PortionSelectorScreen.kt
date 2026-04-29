package com.appfood.shared.ui.journal

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.Aliment
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.PortionStandard
import com.appfood.shared.ui.Strings

// Back arrow icon
private val BackArrowIcon: ImageVector by lazy {
    ImageVector.Builder("Back", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            close()
        }.build()
}

// Quick quantity suggestions in grams
private val QUICK_QUANTITIES = listOf(50.0, 100.0, 150.0, 200.0, 250.0)

// Generic portions available for all foods
private val GENERIC_PORTIONS = listOf(
    GenericPortion(Strings.JOURNAL_PORTION_CUP, 250.0),
    GenericPortion(Strings.JOURNAL_PORTION_TABLESPOON, 15.0),
    GenericPortion(Strings.JOURNAL_PORTION_TEASPOON, 5.0),
    GenericPortion(Strings.JOURNAL_PORTION_HANDFUL, 30.0),
)

private data class GenericPortion(val label: String, val grams: Double)

/**
 * Portion selector screen (JOURNAL-01, PORTIONS-01).
 * Allows user to select portion size via grammes input, quick suggestions,
 * food-specific portions, or generic portions.
 * Shows nutritional summary before validation.
 */
@Composable
fun PortionSelectorScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    onEntryValidated: () -> Unit,
) {
    val selectedAliment by viewModel.selectedAliment.collectAsState()
    val quantityGrams by viewModel.quantityGrams.collectAsState()
    val selectedPortion by viewModel.selectedPortion.collectAsState()
    val addEntryState by viewModel.addEntryState.collectAsState()
    val nutritionPreview by viewModel.nutritionPreview.collectAsState()
    val loadedPortions by viewModel.loadedPortions.collectAsState()

    val aliment = selectedAliment ?: return

    // Navigate on save success (online or offline).
    // IMPORTANT : on declenche la nav AVANT le reset, sinon le reset remet
    // le state a SelectMeal et on perd l'evenement.
    LaunchedEffect(addEntryState) {
        val current = addEntryState
        if (current is AddEntryState.Saved || current is AddEntryState.SavedOffline) {
            onEntryValidated()
            viewModel.resetAddEntryFlow()
        }
    }

    PortionSelectorContent(
        aliment = aliment,
        quantityGrams = quantityGrams,
        selectedPortion = selectedPortion,
        loadedPortions = loadedPortions,
        computedNutriments = nutritionPreview ?: NutrimentValues(),
        isSaving = addEntryState is AddEntryState.Saving,
        errorMessage = (addEntryState as? AddEntryState.Error)?.message,
        onQuantityChanged = viewModel::onQuantityChanged,
        onPortionSelected = viewModel::onPortionSelected,
        onQuickQuantitySelected = viewModel::onQuickQuantitySelected,
        onValidate = {
            viewModel.onValidateEntry()
        },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PortionSelectorContent(
    aliment: Aliment,
    quantityGrams: Double,
    selectedPortion: PortionStandard?,
    loadedPortions: List<PortionStandard>,
    computedNutriments: NutrimentValues,
    isSaving: Boolean,
    errorMessage: String?,
    onQuantityChanged: (Double) -> Unit,
    onPortionSelected: (PortionStandard) -> Unit,
    onQuickQuantitySelected: (Double) -> Unit,
    onValidate: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var quantityText by remember(quantityGrams) {
        mutableStateOf(
            if (quantityGrams == quantityGrams.toLong().toDouble()) {
                quantityGrams.toLong().toString()
            } else {
                quantityGrams.toString()
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(aliment.nom) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackArrowIcon,
                            contentDescription = Strings.JOURNAL_BACK,
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Aliment info
            if (aliment.marque != null) {
                Text(
                    text = aliment.marque,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Grams input
            Text(
                text = Strings.JOURNAL_PORTION_QUANTITY_LABEL,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            OutlinedTextField(
                value = quantityText,
                onValueChange = { value ->
                    quantityText = value
                    value.toDoubleOrNull()?.let { onQuantityChanged(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(Strings.JOURNAL_PORTION_GRAMS) },
                suffix = { Text("g") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
            )

            // Quick quantity suggestions
            Text(
                text = Strings.JOURNAL_PORTION_QUICK_SUGGESTIONS,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QUICK_QUANTITIES.forEach { grams ->
                    FilterChip(
                        selected = quantityGrams == grams && selectedPortion == null,
                        onClick = { onQuickQuantitySelected(grams) },
                        label = { Text("${grams.toLong()}g") },
                    )
                }
            }

            // Portions suggerees (UX-07 — chargees depuis l'API par nom d'aliment)
            if (loadedPortions.isNotEmpty()) {
                Text(
                    text = Strings.JOURNAL_PORTION_STANDARD,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    loadedPortions.forEach { portion ->
                        FilterChip(
                            selected = selectedPortion?.id == portion.id,
                            onClick = { onPortionSelected(portion) },
                            label = {
                                Text("${portion.nom} (${portion.quantiteGrammes.toLong()}g)")
                            },
                        )
                    }
                }
            }

            // Generic portions (always available)
            Text(
                text = Strings.JOURNAL_PORTION_GENERIC,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GENERIC_PORTIONS.forEach { portion ->
                    FilterChip(
                        selected = quantityGrams == portion.grams && selectedPortion == null,
                        onClick = { onQuickQuantitySelected(portion.grams) },
                        label = { Text("${portion.label} (${portion.grams.toLong()}g)") },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Nutritional summary
            Text(
                text = Strings.JOURNAL_NUTRITION_SUMMARY,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            NutritionSummaryCard(
                nutriments = computedNutriments,
                quantityGrams = quantityGrams,
            )

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Validate button
            Button(
                onClick = onValidate,
                enabled = !isSaving && quantityGrams > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(24.dp),
                    )
                } else {
                    Text(Strings.JOURNAL_VALIDATE_ENTRY)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NutritionSummaryCard(
    nutriments: NutrimentValues,
    quantityGrams: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = Strings.journalPortionSummaryFor(quantityGrams),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            NutrimentRow(Strings.JOURNAL_NUTRIMENT_CALORIES, formatCalories(nutriments.calories), Strings.JOURNAL_UNIT_KCAL)
            NutrimentRow(Strings.JOURNAL_NUTRIMENT_PROTEINES, formatNutriment(nutriments.proteines), Strings.JOURNAL_UNIT_G)
            NutrimentRow(Strings.JOURNAL_NUTRIMENT_GLUCIDES, formatNutriment(nutriments.glucides), Strings.JOURNAL_UNIT_G)
            NutrimentRow(Strings.JOURNAL_NUTRIMENT_LIPIDES, formatNutriment(nutriments.lipides), Strings.JOURNAL_UNIT_G)
            NutrimentRow(Strings.JOURNAL_NUTRIMENT_FIBRES, formatNutriment(nutriments.fibres), Strings.JOURNAL_UNIT_G)
        }
    }
}

@Composable
private fun NutrimentRow(
    label: String,
    value: String,
    unit: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatCalories(value: Double): String {
    val rounded = kotlin.math.round(value).toLong()
    return rounded.toString()
}

private fun formatNutriment(value: Double): String {
    if (value < 0.1) return "< 0.1"
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
