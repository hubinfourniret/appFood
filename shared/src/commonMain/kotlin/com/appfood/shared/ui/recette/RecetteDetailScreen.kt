package com.appfood.shared.ui.recette

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.IngredientRecette
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentValues
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

// Heart icon (filled)
private val HeartFilledIcon: ImageVector by lazy {
    ImageVector.Builder("HeartFilled", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Red)) {
            moveTo(12f, 21.35f)
            lineTo(10.55f, 20.03f)
            curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
            curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
            curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
            curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
            curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
            curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
            lineTo(12f, 21.35f)
            close()
        }.build()
}

// Heart icon (outline)
private val HeartOutlineIcon: ImageVector by lazy {
    ImageVector.Builder("HeartOutline", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Gray)) {
            moveTo(16.5f, 3f)
            curveTo(14.76f, 3f, 13.09f, 3.81f, 12f, 5.09f)
            curveTo(10.91f, 3.81f, 9.24f, 3f, 7.5f, 3f)
            curveTo(4.42f, 3f, 2f, 5.42f, 2f, 8.5f)
            curveTo(2f, 12.28f, 5.4f, 15.36f, 10.55f, 20.04f)
            lineTo(12f, 21.35f)
            lineTo(13.45f, 20.03f)
            curveTo(18.6f, 15.36f, 22f, 12.28f, 22f, 8.5f)
            curveTo(22f, 5.42f, 19.58f, 3f, 16.5f, 3f)
            close()
            moveTo(12.1f, 18.55f)
            lineTo(12f, 18.65f)
            lineTo(11.9f, 18.55f)
            curveTo(7.14f, 14.24f, 4f, 11.39f, 4f, 8.5f)
            curveTo(4f, 6.5f, 5.5f, 5f, 7.5f, 5f)
            curveTo(9.04f, 5f, 10.54f, 5.99f, 11.07f, 7.36f)
            horizontalLineTo(12.94f)
            curveTo(13.46f, 5.99f, 14.96f, 5f, 16.5f, 5f)
            curveTo(18.5f, 5f, 20f, 6.5f, 20f, 8.5f)
            curveTo(20f, 11.39f, 16.86f, 14.24f, 12.1f, 18.55f)
            close()
        }.build()
}

// Plus/Minus icons
private val AddIcon: ImageVector by lazy {
    ImageVector.Builder("Add", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            horizontalLineTo(13f)
            verticalLineTo(19f)
            horizontalLineTo(11f)
            verticalLineTo(13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(11f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            close()
        }.build()
}

private val RemoveIcon: ImageVector by lazy {
    ImageVector.Builder("Remove", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            close()
        }.build()
}

/**
 * Recipe detail screen (RECETTES-02).
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun RecetteDetailScreen(
    recetteId: String,
    viewModel: RecettesViewModel,
    onNavigateBack: () -> Unit,
) {
    val detailState by viewModel.detailState.collectAsState()
    val selectedPortions by viewModel.selectedPortions.collectAsState()
    val isFavorite by viewModel.isDetailFavorite.collectAsState()
    val showMealDialog by viewModel.showMealSelectionDialog.collectAsState()
    val addToJournalState by viewModel.addToJournalState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(recetteId) {
        viewModel.loadRecetteDetail(recetteId)
    }

    // Show snackbar on success/offline save
    LaunchedEffect(addToJournalState) {
        when (addToJournalState) {
            is AddRecetteToJournalState.Success -> {
                snackbarHostState.showSnackbar(Strings.RECETTE_ADDED_TO_JOURNAL)
                viewModel.resetAddToJournalState()
            }
            is AddRecetteToJournalState.SavedOffline -> {
                snackbarHostState.showSnackbar(Strings.RECETTE_ADDED_TO_JOURNAL_OFFLINE)
                viewModel.resetAddToJournalState()
            }
            else -> {}
        }
    }

    // Meal selection dialog
    if (showMealDialog) {
        MealSelectionDialog(
            onMealSelected = viewModel::onMealSelectedForRecette,
            onDismiss = viewModel::onDismissMealSelectionDialog,
        )
    }

    RecetteDetailContent(
        state = detailState,
        selectedPortions = selectedPortions,
        isFavorite = isFavorite,
        onPortionsChanged = viewModel::onDetailPortionsChanged,
        onToggleFavorite = viewModel::onToggleDetailFavorite,
        onAddToJournal = viewModel::onAddRecetteToJournal,
        onRetry = { viewModel.loadRecetteDetail(recetteId) },
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

/**
 * Dialog asking the user to select a meal type before adding the recipe to the journal.
 */
@Composable
private fun MealSelectionDialog(
    onMealSelected: (MealType) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.JOURNAL_SELECT_MEAL_TYPE) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MealOptionButton(
                    label = Strings.JOURNAL_MEAL_BREAKFAST,
                    onClick = { onMealSelected(MealType.PETIT_DEJEUNER) },
                )
                MealOptionButton(
                    label = Strings.JOURNAL_MEAL_LUNCH,
                    onClick = { onMealSelected(MealType.DEJEUNER) },
                )
                MealOptionButton(
                    label = Strings.JOURNAL_MEAL_DINNER,
                    onClick = { onMealSelected(MealType.DINER) },
                )
                MealOptionButton(
                    label = Strings.JOURNAL_MEAL_SNACK,
                    onClick = { onMealSelected(MealType.COLLATION) },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.RECETTE_ADD_CANCEL)
            }
        },
    )
}

@Composable
private fun MealOptionButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecetteDetailContent(
    state: RecetteDetailState,
    selectedPortions: Int,
    isFavorite: Boolean,
    onPortionsChanged: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToJournal: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(Strings.RECETTE_DETAIL_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackArrowIcon,
                            contentDescription = Strings.JOURNAL_BACK,
                        )
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = isFavorite,
                        onCheckedChange = { onToggleFavorite() },
                    ) {
                        Icon(
                            imageVector = if (isFavorite) HeartFilledIcon else HeartOutlineIcon,
                            contentDescription = if (isFavorite) {
                                Strings.RECETTE_FAVORIS_REMOVE
                            } else {
                                Strings.RECETTE_FAVORIS_ADD
                            },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            is RecetteDetailState.Loading -> {
                com.appfood.shared.ui.common.LoadingSkeleton(
                    lines = 8,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is RecetteDetailState.Error -> {
                com.appfood.shared.ui.common.ErrorMessage(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is RecetteDetailState.Success -> {
                RecetteDetailBody(
                    recette = state,
                    selectedPortions = selectedPortions,
                    onPortionsChanged = onPortionsChanged,
                    onAddToJournal = onAddToJournal,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun RecetteDetailBody(
    recette: RecetteDetailState.Success,
    selectedPortions: Int,
    onPortionsChanged: (Int) -> Unit,
    onAddToJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Name and description
        Text(
            text = recette.nom,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (recette.description.isNotBlank()) {
            Text(
                text = recette.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Prep and cooking time
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TimeInfo(
                label = Strings.RECETTE_TEMPS_PREP_LABEL,
                minutes = recette.tempsPreparationMin,
            )
            TimeInfo(
                label = Strings.RECETTE_TEMPS_CUISSON_LABEL,
                minutes = recette.tempsCuissonMin,
            )
        }

        // Portion selector
        PortionSelectorRow(
            selectedPortions = selectedPortions,
            onPortionsChanged = onPortionsChanged,
        )

        Divider()

        // Ingredients
        IngredientsSection(
            ingredients = recette.ingredients,
            portionFactor = selectedPortions.toDouble() / recette.nbPortions.toDouble(),
        )

        Divider()

        // Preparation steps
        EtapesSection(etapes = recette.etapes)

        Divider()

        // Nutritional table
        NutritionSection(
            nutriments = recette.nutrimentsTotaux,
            nbPortionsOriginal = recette.nbPortions,
        )

        // Add to journal button
        Button(
            onClick = onAddToJournal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(Strings.RECETTE_ADD_TO_JOURNAL)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TimeInfo(
    label: String,
    minutes: Int,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = Strings.recetteTempsPrep(minutes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun PortionSelectorRow(
    selectedPortions: Int,
    onPortionsChanged: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = Strings.RECETTE_PORTIONS_LABEL,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { if (selectedPortions > 1) onPortionsChanged(selectedPortions - 1) },
                enabled = selectedPortions > 1,
            ) {
                Icon(
                    imageVector = RemoveIcon,
                    contentDescription = "-",
                )
            }
            Text(
                text = selectedPortions.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(
                onClick = { onPortionsChanged(selectedPortions + 1) },
            ) {
                Icon(
                    imageVector = AddIcon,
                    contentDescription = "+",
                )
            }
        }
    }
}

@Composable
private fun IngredientsSection(
    ingredients: List<IngredientRecette>,
    portionFactor: Double,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = Strings.RECETTE_INGREDIENTS_TITLE,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        ingredients.forEach { ingredient ->
            val adjustedQuantity = ingredient.quantiteGrammes * portionFactor
            Text(
                text = Strings.recetteIngredientQuantity(
                    ingredient.alimentNom,
                    adjustedQuantity,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun EtapesSection(etapes: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = Strings.RECETTE_ETAPES_TITLE,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        etapes.forEachIndexed { index, etape ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = Strings.recetteEtapeNumber(index + 1),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = etape,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NutritionSection(
    nutriments: NutrimentValues,
    nbPortionsOriginal: Int,
) {
    val perPortion = { value: Double -> value / nbPortionsOriginal }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = Strings.RECETTE_NUTRITION_TITLE,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NutrimentRow(Strings.JOURNAL_NUTRIMENT_CALORIES, perPortion(nutriments.calories), Strings.JOURNAL_UNIT_KCAL)
                NutrimentRow(Strings.JOURNAL_NUTRIMENT_PROTEINES, perPortion(nutriments.proteines), Strings.JOURNAL_UNIT_G)
                NutrimentRow(Strings.JOURNAL_NUTRIMENT_GLUCIDES, perPortion(nutriments.glucides), Strings.JOURNAL_UNIT_G)
                NutrimentRow(Strings.JOURNAL_NUTRIMENT_LIPIDES, perPortion(nutriments.lipides), Strings.JOURNAL_UNIT_G)
                NutrimentRow(Strings.JOURNAL_NUTRIMENT_FIBRES, perPortion(nutriments.fibres), Strings.JOURNAL_UNIT_G)
                NutrimentRow("Fer", perPortion(nutriments.fer), "mg")
                NutrimentRow("Calcium", perPortion(nutriments.calcium), "mg")
                NutrimentRow("Vitamine B12", perPortion(nutriments.vitamineB12), "ug")
                NutrimentRow("Zinc", perPortion(nutriments.zinc), "mg")
                NutrimentRow("Omega-3", perPortion(nutriments.omega3), Strings.JOURNAL_UNIT_G)
            }
        }
    }
}

@Composable
private fun NutrimentRow(
    label: String,
    value: Double,
    unit: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${((value * 10).toInt() / 10.0)} $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
