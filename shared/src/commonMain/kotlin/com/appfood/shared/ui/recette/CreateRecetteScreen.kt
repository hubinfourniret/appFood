package com.appfood.shared.ui.recette

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.RegimeAlimentaire
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

// Delete icon
private val DeleteIcon: ImageVector by lazy {
    ImageVector.Builder("Delete", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 19f)
            curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
            horizontalLineTo(16f)
            curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
            verticalLineTo(7f)
            horizontalLineTo(6f)
            close()
            moveTo(19f, 4f)
            horizontalLineTo(15.5f)
            lineTo(14.5f, 3f)
            horizontalLineTo(9.5f)
            lineTo(8.5f, 4f)
            horizontalLineTo(5f)
            verticalLineTo(6f)
            horizontalLineTo(19f)
            close()
        }.build()
}

/**
 * Admin recipe creation screen (RECETTES-03).
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun CreateRecetteScreen(
    viewModel: RecettesViewModel,
    onNavigateBack: () -> Unit,
    editRecetteId: String? = null,
) {
    val createState by viewModel.createRecetteState.collectAsState()
    val formState by viewModel.createRecetteForm.collectAsState()
    val alimentPickerForIndex by viewModel.alimentPickerForIndex.collectAsState()
    val alimentPickerQuery by viewModel.alimentPickerQuery.collectAsState()
    val alimentPickerResults by viewModel.alimentPickerResults.collectAsState()
    val alimentPickerLoading by viewModel.alimentPickerLoading.collectAsState()

    LaunchedEffect(editRecetteId) {
        if (editRecetteId != null) {
            viewModel.startEditRecette(editRecetteId)
        } else {
            viewModel.startCreateRecette()
        }
    }

    CreateRecetteContent(
        state = createState,
        form = formState,
        onNomChanged = viewModel::onCreateNomChanged,
        onDescriptionChanged = viewModel::onCreateDescriptionChanged,
        onImageUrlChanged = viewModel::onCreateImageUrlChanged,
        onTempsPrepChanged = viewModel::onCreateTempsPrepChanged,
        onTempsCuissonChanged = viewModel::onCreateTempsCuissonChanged,
        onNbPortionsChanged = viewModel::onCreateNbPortionsChanged,
        onRegimeChanged = viewModel::onCreateRegimeChanged,
        onAddIngredient = viewModel::onCreateAddIngredient,
        onRemoveIngredient = viewModel::onCreateRemoveIngredient,
        onPickAliment = viewModel::openAlimentPicker,
        onIngredientQuantityChanged = viewModel::onCreateIngredientQuantityChanged,
        onAddEtape = viewModel::onCreateAddEtape,
        onRemoveEtape = viewModel::onCreateRemoveEtape,
        onEtapeChanged = viewModel::onCreateEtapeChanged,
        onMoveEtapeUp = viewModel::onCreateMoveEtapeUp,
        onMoveEtapeDown = viewModel::onCreateMoveEtapeDown,
        onSubmit = viewModel::onCreateRecetteSubmit,
        onNavigateBack = onNavigateBack,
    )

    if (alimentPickerForIndex != null) {
        AlimentPickerDialog(
            query = alimentPickerQuery,
            results = alimentPickerResults,
            loading = alimentPickerLoading,
            onQueryChanged = viewModel::onAlimentPickerQueryChanged,
            onAlimentSelected = viewModel::onAlimentPickerSelected,
            onDismiss = viewModel::closeAlimentPicker,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRecetteContent(
    state: CreateRecetteState,
    form: CreateRecetteFormState,
    onNomChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onTempsPrepChanged: (String) -> Unit,
    onTempsCuissonChanged: (String) -> Unit,
    onNbPortionsChanged: (String) -> Unit,
    onRegimeChanged: (RegimeAlimentaire) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onPickAliment: (Int) -> Unit,
    onIngredientQuantityChanged: (Int, String) -> Unit,
    onAddEtape: () -> Unit,
    onRemoveEtape: (Int) -> Unit,
    onEtapeChanged: (Int, String) -> Unit,
    onMoveEtapeUp: (Int) -> Unit,
    onMoveEtapeDown: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state is CreateRecetteState.Success) {
            snackbarHostState.showSnackbar(Strings.RECETTE_CREATE_SUCCESS)
            onNavigateBack()
        }
        if (state is CreateRecetteState.Error) {
            snackbarHostState.showSnackbar(state.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.RECETTE_CREATE_TITLE) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Name
            OutlinedTextField(
                value = form.nom,
                onValueChange = onNomChanged,
                label = { Text(Strings.RECETTE_CREATE_NOM_LABEL) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Description
            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChanged,
                label = { Text(Strings.RECETTE_CREATE_DESCRIPTION_LABEL) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            // Image URL
            OutlinedTextField(
                value = form.imageUrl,
                onValueChange = onImageUrlChanged,
                label = { Text(Strings.RECETTE_CREATE_IMAGE_URL_LABEL) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Time fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = form.tempsPrepMin,
                    onValueChange = onTempsPrepChanged,
                    label = { Text(Strings.RECETTE_CREATE_TEMPS_PREP_LABEL) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = form.tempsCuissonMin,
                    onValueChange = onTempsCuissonChanged,
                    label = { Text(Strings.RECETTE_CREATE_TEMPS_CUISSON_LABEL) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // Nb portions
            OutlinedTextField(
                value = form.nbPortions,
                onValueChange = onNbPortionsChanged,
                label = { Text(Strings.RECETTE_CREATE_NB_PORTIONS_LABEL) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(160.dp),
            )

            // Regime dropdown
            RegimeDropdown(
                selectedRegime = form.regime,
                onRegimeChanged = onRegimeChanged,
            )

            // Ingredients section
            IngredientsFormSection(
                ingredients = form.ingredients,
                onAddIngredient = onAddIngredient,
                onRemoveIngredient = onRemoveIngredient,
                onPickAliment = onPickAliment,
                onIngredientQuantityChanged = onIngredientQuantityChanged,
            )

            // Etapes section
            EtapesFormSection(
                etapes = form.etapes,
                onAddEtape = onAddEtape,
                onRemoveEtape = onRemoveEtape,
                onEtapeChanged = onEtapeChanged,
                onMoveEtapeUp = onMoveEtapeUp,
                onMoveEtapeDown = onMoveEtapeDown,
            )

            // Submit
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is CreateRecetteState.Saving && form.nom.isNotBlank(),
            ) {
                Text(Strings.RECETTE_CREATE_SUBMIT)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegimeDropdown(
    selectedRegime: RegimeAlimentaire,
    onRegimeChanged: (RegimeAlimentaire) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val label = when (selectedRegime) {
        RegimeAlimentaire.VEGAN -> Strings.RECETTE_REGIME_VEGAN
        RegimeAlimentaire.VEGETARIEN -> Strings.RECETTE_REGIME_VEGETARIEN
        RegimeAlimentaire.FLEXITARIEN -> Strings.RECETTE_REGIME_FLEXITARIEN
        RegimeAlimentaire.OMNIVORE -> Strings.RECETTE_REGIME_OMNIVORE
    }

    Column {
        Text(
            text = Strings.RECETTE_CREATE_REGIME_LABEL,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            RegimeAlimentaire.entries.forEach { regime ->
                val regimeLabel = when (regime) {
                    RegimeAlimentaire.VEGAN -> Strings.RECETTE_REGIME_VEGAN
                    RegimeAlimentaire.VEGETARIEN -> Strings.RECETTE_REGIME_VEGETARIEN
                    RegimeAlimentaire.FLEXITARIEN -> Strings.RECETTE_REGIME_FLEXITARIEN
                    RegimeAlimentaire.OMNIVORE -> Strings.RECETTE_REGIME_OMNIVORE
                }
                DropdownMenuItem(
                    text = { Text(regimeLabel) },
                    onClick = {
                        onRegimeChanged(regime)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun IngredientsFormSection(
    ingredients: List<IngredientFormEntry>,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onPickAliment: (Int) -> Unit,
    onIngredientQuantityChanged: (Int, String) -> Unit,
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

        ingredients.forEachIndexed { index, ingredient ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // TACHE-516 : bouton qui ouvre le picker aliments (pas de saisie libre)
                    val hasAliment = ingredient.alimentId.isNotBlank()
                    OutlinedButton(
                        onClick = { onPickAliment(index) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (hasAliment) ingredient.alimentNom else Strings.RECETTE_CREATE_INGREDIENT_PICK,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = ingredient.quantiteGrammes,
                            onValueChange = { onIngredientQuantityChanged(index, it) },
                            label = { Text(Strings.RECETTE_CREATE_INGREDIENT_QUANTITY) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onRemoveIngredient(index) }) {
                            Icon(
                                imageVector = DeleteIcon,
                                contentDescription = Strings.RECETTE_CREATE_REMOVE_INGREDIENT,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        TextButton(onClick = onAddIngredient) {
            Text(Strings.RECETTE_CREATE_ADD_INGREDIENT)
        }
    }
}

@Composable
private fun AlimentPickerDialog(
    query: String,
    results: List<AlimentPickerItem>,
    loading: Boolean,
    onQueryChanged: (String) -> Unit,
    onAlimentSelected: (AlimentPickerItem) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.RECETTE_CREATE_INGREDIENT_PICK) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    placeholder = { Text(Strings.JOURNAL_SEARCH_PLACEHOLDER) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (loading && results.isEmpty()) {
                    Text(
                        text = Strings.LOADING,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(items = results, key = { it.id }) { aliment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAlimentSelected(aliment) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = aliment.nom,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "${aliment.categorie} · ${aliment.caloriesPour100g.toInt()} kcal/100g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.JOURNAL_DELETE_CANCEL) }
        },
    )
}

@Composable
private fun EtapesFormSection(
    etapes: List<String>,
    onAddEtape: () -> Unit,
    onRemoveEtape: (Int) -> Unit,
    onEtapeChanged: (Int, String) -> Unit,
    onMoveEtapeUp: (Int) -> Unit,
    onMoveEtapeDown: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = Strings.RECETTE_ETAPES_TITLE,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        etapes.forEachIndexed { index, etape ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = Strings.recetteEtapeNumber(index + 1),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OutlinedTextField(
                        value = etape,
                        onValueChange = { onEtapeChanged(index, it) },
                        placeholder = { Text(Strings.RECETTE_CREATE_ETAPE_PLACEHOLDER) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (index > 0) {
                            TextButton(onClick = { onMoveEtapeUp(index) }) {
                                Text(Strings.RECETTE_CREATE_MOVE_UP)
                            }
                        }
                        if (index < etapes.lastIndex) {
                            TextButton(onClick = { onMoveEtapeDown(index) }) {
                                Text(Strings.RECETTE_CREATE_MOVE_DOWN)
                            }
                        }
                        IconButton(onClick = { onRemoveEtape(index) }) {
                            Icon(
                                imageVector = DeleteIcon,
                                contentDescription = Strings.RECETTE_CREATE_REMOVE_ETAPE,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        TextButton(onClick = onAddEtape) {
            Text(Strings.RECETTE_CREATE_ADD_ETAPE)
        }
    }
}
