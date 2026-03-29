package com.appfood.shared.ui.recette

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.MealType
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.EmptyState
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton

/**
 * Recipe list screen (RECETTES-01).
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun RecettesListScreen(
    viewModel: RecettesViewModel,
    onRecetteClick: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedRegimes by viewModel.selectedRegimes.collectAsState()
    val selectedMealTypes by viewModel.selectedMealTypes.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    RecettesListContent(
        state = state,
        searchQuery = searchQuery,
        selectedRegimes = selectedRegimes,
        selectedMealTypes = selectedMealTypes,
        sortOption = sortOption,
        hasMore = hasMore,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onRegimeToggle = viewModel::onRegimeToggle,
        onMealTypeToggle = viewModel::onMealTypeToggle,
        onSortChanged = viewModel::onSortChanged,
        onClearFilters = viewModel::onClearFilters,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
        onRecetteClick = onRecetteClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RecettesListContent(
    state: RecettesState,
    searchQuery: String,
    selectedRegimes: Set<RegimeAlimentaire>,
    selectedMealTypes: Set<MealType>,
    sortOption: RecetteSortOption,
    hasMore: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onRegimeToggle: (RegimeAlimentaire) -> Unit,
    onMealTypeToggle: (MealType) -> Unit,
    onSortChanged: (RecetteSortOption) -> Unit,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onRecetteClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.SCREEN_RECETTES) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text(Strings.RECETTE_SEARCH_PLACEHOLDER) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Filters section
            FiltersSection(
                selectedRegimes = selectedRegimes,
                selectedMealTypes = selectedMealTypes,
                sortOption = sortOption,
                onRegimeToggle = onRegimeToggle,
                onMealTypeToggle = onMealTypeToggle,
                onSortChanged = onSortChanged,
                onClearFilters = onClearFilters,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content
            when (state) {
                is RecettesState.Loading -> {
                    LoadingSkeleton(lines = 6)
                }

                is RecettesState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = onRetry,
                    )
                }

                is RecettesState.Success -> {
                    if (state.recettes.isEmpty()) {
                        EmptyState(message = Strings.RECETTE_EMPTY)
                    } else {
                        RecettesList(
                            recettes = state.recettes,
                            hasMore = hasMore,
                            onLoadMore = onLoadMore,
                            onRecetteClick = onRecetteClick,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersSection(
    selectedRegimes: Set<RegimeAlimentaire>,
    selectedMealTypes: Set<MealType>,
    sortOption: RecetteSortOption,
    onRegimeToggle: (RegimeAlimentaire) -> Unit,
    onMealTypeToggle: (MealType) -> Unit,
    onSortChanged: (RecetteSortOption) -> Unit,
    onClearFilters: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Regime filters
        Text(
            text = Strings.RECETTE_FILTER_REGIME,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RegimeAlimentaire.entries.forEach { regime ->
                val label = when (regime) {
                    RegimeAlimentaire.VEGAN -> Strings.RECETTE_REGIME_VEGAN
                    RegimeAlimentaire.VEGETARIEN -> Strings.RECETTE_REGIME_VEGETARIEN
                    RegimeAlimentaire.FLEXITARIEN -> Strings.RECETTE_REGIME_FLEXITARIEN
                    RegimeAlimentaire.OMNIVORE -> Strings.RECETTE_REGIME_OMNIVORE
                }
                FilterChip(
                    selected = regime in selectedRegimes,
                    onClick = { onRegimeToggle(regime) },
                    label = { Text(label) },
                )
            }
        }

        // Meal type filters
        Text(
            text = Strings.RECETTE_FILTER_TYPE_REPAS,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MealType.entries.forEach { mealType ->
                val label = when (mealType) {
                    MealType.PETIT_DEJEUNER -> Strings.JOURNAL_MEAL_BREAKFAST
                    MealType.DEJEUNER -> Strings.JOURNAL_MEAL_LUNCH
                    MealType.DINER -> Strings.JOURNAL_MEAL_DINNER
                    MealType.COLLATION -> Strings.JOURNAL_MEAL_SNACK
                }
                FilterChip(
                    selected = mealType in selectedMealTypes,
                    onClick = { onMealTypeToggle(mealType) },
                    label = { Text(label) },
                )
            }
        }

        // Sort + Clear row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SortDropdown(
                sortOption = sortOption,
                onSortChanged = onSortChanged,
            )
            TextButton(onClick = onClearFilters) {
                Text(Strings.RECETTE_CLEAR_FILTERS)
            }
        }
    }
}

@Composable
private fun SortDropdown(
    sortOption: RecetteSortOption,
    onSortChanged: (RecetteSortOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val label = when (sortOption) {
        RecetteSortOption.PERTINENCE -> Strings.RECETTE_SORT_PERTINENCE
        RecetteSortOption.POPULARITE -> Strings.RECETTE_SORT_POPULARITE
        RecetteSortOption.TEMPS_PREPARATION -> Strings.RECETTE_SORT_TEMPS
    }

    TextButton(onClick = { expanded = true }) {
        Text("${Strings.RECETTE_SORT_LABEL} $label")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        RecetteSortOption.entries.forEach { option ->
            val optionLabel = when (option) {
                RecetteSortOption.PERTINENCE -> Strings.RECETTE_SORT_PERTINENCE
                RecetteSortOption.POPULARITE -> Strings.RECETTE_SORT_POPULARITE
                RecetteSortOption.TEMPS_PREPARATION -> Strings.RECETTE_SORT_TEMPS
            }
            DropdownMenuItem(
                text = { Text(optionLabel) },
                onClick = {
                    onSortChanged(option)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun RecettesList(
    recettes: List<com.appfood.shared.model.Recette>,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onRecetteClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    // Detect end of list for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && hasMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = recettes,
            key = { it.id },
        ) { recette ->
            RecetteCard(
                recette = recette,
                onClick = { onRecetteClick(recette.id) },
            )
        }
    }
}
