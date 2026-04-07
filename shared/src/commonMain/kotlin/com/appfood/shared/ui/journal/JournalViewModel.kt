package com.appfood.shared.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.model.Aliment
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.PortionStandard
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the entire journal feature (JOURNAL-01, JOURNAL-03, JOURNAL-04, JOURNAL-06, PORTIONS-01).
 * Manages food search, entry creation, favorites, recents, edit and delete.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class JournalViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val ajouterEntreeUseCase: AjouterEntreeUseCase,
    // private val calculerApportsUseCase: CalculerApportsUseCase,
    // private val rechercherAlimentUseCase: RechercherAlimentUseCase,
    // private val getFavorisUseCase: GetFavorisUseCase,
    // private val toggleFavoriUseCase: ToggleFavoriUseCase,
    // private val getRecentsUseCase: GetRecentsUseCase,
    // private val modifierEntreeUseCase: ModifierEntreeUseCase,
    // private val supprimerEntreeUseCase: SupprimerEntreeUseCase,
    // private val getPortionsUseCase: GetPortionsUseCase,
) : ViewModel() {

    // --- Search state ---
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Entry creation state ---
    private val _addEntryState = MutableStateFlow<AddEntryState>(AddEntryState.SelectMeal)
    val addEntryState: StateFlow<AddEntryState> = _addEntryState.asStateFlow()

    private val _selectedMealType = MutableStateFlow<MealType?>(null)
    val selectedMealType: StateFlow<MealType?> = _selectedMealType.asStateFlow()

    private val _selectedAliment = MutableStateFlow<Aliment?>(null)
    val selectedAliment: StateFlow<Aliment?> = _selectedAliment.asStateFlow()

    private val _quantityGrams = MutableStateFlow(DEFAULT_QUANTITY)
    val quantityGrams: StateFlow<Double> = _quantityGrams.asStateFlow()

    private val _selectedPortion = MutableStateFlow<PortionStandard?>(null)
    val selectedPortion: StateFlow<PortionStandard?> = _selectedPortion.asStateFlow()

    // --- Favorites ---
    private val _favorites = MutableStateFlow<List<Aliment>>(emptyList())
    val favorites: StateFlow<List<Aliment>> = _favorites.asStateFlow()

    // --- Recents ---
    private val _recents = MutableStateFlow<List<RecentEntry>>(emptyList())
    val recents: StateFlow<List<RecentEntry>> = _recents.asStateFlow()

    // --- Recette search state (JOURNAL-02) ---
    private val _recetteSearchQuery = MutableStateFlow("")
    val recetteSearchQuery: StateFlow<String> = _recetteSearchQuery.asStateFlow()

    private val _recetteSearchResults = MutableStateFlow<List<RecetteSearchResult>>(emptyList())
    val recetteSearchResults: StateFlow<List<RecetteSearchResult>> = _recetteSearchResults.asStateFlow()

    // --- Edit/Delete state ---
    private val _editState = MutableStateFlow<EditEntryState>(EditEntryState.Idle)
    val editState: StateFlow<EditEntryState> = _editState.asStateFlow()

    // --- Nutritional summary for current selection ---
    val computedNutriments: NutrimentValues
        get() {
            val aliment = _selectedAliment.value ?: return NutrimentValues()
            val grams = _quantityGrams.value
            val factor = grams / 100.0
            val n = aliment.nutrimentsPour100g
            return NutrimentValues(
                calories = n.calories * factor,
                proteines = n.proteines * factor,
                glucides = n.glucides * factor,
                lipides = n.lipides * factor,
                fibres = n.fibres * factor,
                sel = n.sel * factor,
                sucres = n.sucres * factor,
                fer = n.fer * factor,
                calcium = n.calcium * factor,
                zinc = n.zinc * factor,
                magnesium = n.magnesium * factor,
                vitamineB12 = n.vitamineB12 * factor,
                vitamineD = n.vitamineD * factor,
                vitamineC = n.vitamineC * factor,
                omega3 = n.omega3 * factor,
                omega6 = n.omega6 * factor,
            )
        }

    @OptIn(FlowPreview::class)
    fun init() {
        // Debounce search queries
        _searchQuery
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length >= MIN_SEARCH_LENGTH) {
                    performSearch(query)
                } else if (query.isEmpty()) {
                    _searchState.value = SearchState.Idle
                }
            }
            .launchIn(viewModelScope)

        loadFavorites()
        loadRecents()
    }

    // --- Meal type selection ---

    fun onMealTypeSelected(mealType: MealType) {
        _selectedMealType.value = mealType
        _addEntryState.value = AddEntryState.SearchFood
    }

    // --- Search ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.length < MIN_SEARCH_LENGTH) {
            _searchState.value = SearchState.Idle
        }
    }

    private fun performSearch(query: String) {
        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            // TODO: Call rechercherAlimentUseCase when created by SHARED agent
            // val result = rechercherAlimentUseCase(query)
            // when (result) {
            //     is AppResult.Success -> {
            //         _searchState.value = if (result.data.isEmpty()) {
            //             SearchState.Empty
            //         } else {
            //             SearchState.Results(result.data)
            //         }
            //     }
            //     is AppResult.Error -> {
            //         _searchState.value = SearchState.Error(result.message)
            //     }
            // }

            // Stub: empty results
            _searchState.value = SearchState.Empty
        }
    }

    // --- Aliment selection ---

    fun onAlimentSelected(aliment: Aliment) {
        _selectedAliment.value = aliment
        _quantityGrams.value = DEFAULT_QUANTITY
        _selectedPortion.value = null
        _addEntryState.value = AddEntryState.SelectPortion
    }

    // --- Portion / quantity ---

    fun onQuantityChanged(grams: Double) {
        _quantityGrams.value = grams.coerceIn(MIN_QUANTITY, MAX_QUANTITY)
        _selectedPortion.value = null
    }

    fun onPortionSelected(portion: PortionStandard) {
        _selectedPortion.value = portion
        _quantityGrams.value = portion.quantiteGrammes
    }

    fun onQuickQuantitySelected(grams: Double) {
        _quantityGrams.value = grams
        _selectedPortion.value = null
    }

    // --- Validate entry ---

    fun onValidateEntry() {
        val mealType = _selectedMealType.value ?: return
        val aliment = _selectedAliment.value ?: return
        val grams = _quantityGrams.value

        _addEntryState.value = AddEntryState.Saving
        viewModelScope.launch {
            // TODO: Call ajouterEntreeUseCase when created by SHARED agent
            // val result = ajouterEntreeUseCase(
            //     alimentId = aliment.id,
            //     mealType = mealType,
            //     quantiteGrammes = grams,
            // )
            // when (result) {
            //     is AppResult.Success -> {
            //         _addEntryState.value = AddEntryState.Saved
            //         loadRecents()
            //     }
            //     is AppResult.Error -> {
            //         _addEntryState.value = AddEntryState.Error(result.message)
            //     }
            // }

            // Stub: simulate success
            _addEntryState.value = AddEntryState.Saved
        }
    }

    // --- Favorites (JOURNAL-03) ---

    fun onToggleFavorite(aliment: Aliment) {
        viewModelScope.launch {
            // TODO: Call toggleFavoriUseCase when created by SHARED agent
            // val result = toggleFavoriUseCase(aliment.id)
            // when (result) {
            //     is AppResult.Success -> loadFavorites()
            //     is AppResult.Error -> { /* show error */ }
            // }

            // Stub: toggle locally
            _favorites.update { current ->
                if (current.any { it.id == aliment.id }) {
                    current.filter { it.id != aliment.id }
                } else {
                    current + aliment
                }
            }
        }
    }

    fun isFavorite(alimentId: String): Boolean {
        return _favorites.value.any { it.id == alimentId }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            // TODO: Call getFavorisUseCase when created by SHARED agent
            // val result = getFavorisUseCase()
            // when (result) {
            //     is AppResult.Success -> _favorites.value = result.data
            //     is AppResult.Error -> { /* silent fail, offline data */ }
            // }
        }
    }

    // --- Recents (JOURNAL-04) ---

    fun onRecentEntryTap(entry: RecentEntry) {
        _selectedAliment.value = entry.aliment
        _quantityGrams.value = entry.quantiteGrammes
        _selectedPortion.value = null
        _addEntryState.value = AddEntryState.SelectPortion
    }

    private fun loadRecents() {
        viewModelScope.launch {
            // TODO: Call getRecentsUseCase when created by SHARED agent
            // val result = getRecentsUseCase(limit = MAX_RECENTS)
            // when (result) {
            //     is AppResult.Success -> _recents.value = result.data
            //     is AppResult.Error -> { /* silent fail */ }
            // }
        }
    }

    // --- Edit / Delete (JOURNAL-06) ---

    fun onEditEntry(entryId: String, newQuantityGrams: Double) {
        _editState.value = EditEntryState.Saving
        viewModelScope.launch {
            // TODO: Call modifierEntreeUseCase when created by SHARED agent
            // val result = modifierEntreeUseCase(entryId, newQuantityGrams)
            // when (result) {
            //     is AppResult.Success -> {
            //         _editState.value = EditEntryState.Success
            //     }
            //     is AppResult.Error -> {
            //         _editState.value = EditEntryState.Error(result.message)
            //     }
            // }

            // Stub: simulate success
            _editState.value = EditEntryState.Success
        }
    }

    fun onDeleteEntry(entryId: String) {
        _editState.value = EditEntryState.Saving
        viewModelScope.launch {
            // TODO: Call supprimerEntreeUseCase when created by SHARED agent
            // val result = supprimerEntreeUseCase(entryId)
            // when (result) {
            //     is AppResult.Success -> {
            //         _editState.value = EditEntryState.Deleted
            //     }
            //     is AppResult.Error -> {
            //         _editState.value = EditEntryState.Error(result.message)
            //     }
            // }

            // Stub: simulate success
            _editState.value = EditEntryState.Deleted
        }
    }

    fun resetEditState() {
        _editState.value = EditEntryState.Idle
    }

    // --- Recette search (JOURNAL-02) ---

    fun onRecetteSearchQueryChanged(query: String) {
        _recetteSearchQuery.value = query
        if (query.length < MIN_SEARCH_LENGTH) {
            _recetteSearchResults.value = emptyList()
            return
        }
        performRecetteSearch(query)
    }

    private fun performRecetteSearch(query: String) {
        viewModelScope.launch {
            // TODO: Call recette search use case when created by SHARED agent
            // val result = rechercherRecetteUseCase(query)
            // when (result) {
            //     is AppResult.Success -> {
            //         _recetteSearchResults.value = result.data.map {
            //             RecetteSearchResult(id = it.id, nom = it.nom, tempsPreparationMin = it.tempsPreparationMin)
            //         }
            //     }
            //     is AppResult.Error -> {
            //         _recetteSearchResults.value = emptyList()
            //     }
            // }

            // Stub: empty results
            _recetteSearchResults.value = emptyList()
        }
    }

    fun onRecetteSelected(recetteId: String) {
        viewModelScope.launch {
            // TODO: Navigate to recette detail or add recette portion to journal
            // when created by SHARED agent
        }
    }

    // --- Reset / navigation helpers ---

    fun resetAddEntryFlow() {
        _addEntryState.value = AddEntryState.SelectMeal
        _selectedMealType.value = null
        _selectedAliment.value = null
        _quantityGrams.value = DEFAULT_QUANTITY
        _selectedPortion.value = null
        _searchQuery.value = ""
        _searchState.value = SearchState.Idle
        _recetteSearchQuery.value = ""
        _recetteSearchResults.value = emptyList()
    }

    fun goBackToSearch() {
        _selectedAliment.value = null
        _selectedPortion.value = null
        _quantityGrams.value = DEFAULT_QUANTITY
        _addEntryState.value = AddEntryState.SearchFood
    }

    fun goBackToMealSelection() {
        _selectedMealType.value = null
        _addEntryState.value = AddEntryState.SelectMeal
    }

    companion object {
        private const val DEFAULT_QUANTITY = 100.0
        private const val MIN_QUANTITY = 1.0
        private const val MAX_QUANTITY = 9999.0
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val MIN_SEARCH_LENGTH = 2
        private const val MAX_RECENTS = 20
    }
}

// --- States ---

sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Results(val aliments: List<Aliment>) : SearchState
    data object Empty : SearchState
    data class Error(val message: String) : SearchState
}

sealed interface AddEntryState {
    data object SelectMeal : AddEntryState
    data object SearchFood : AddEntryState
    data object SelectPortion : AddEntryState
    data object Saving : AddEntryState
    data object Saved : AddEntryState
    data class Error(val message: String) : AddEntryState
}

sealed interface EditEntryState {
    data object Idle : EditEntryState
    data object Saving : EditEntryState
    data object Success : EditEntryState
    data object Deleted : EditEntryState
    data class Error(val message: String) : EditEntryState
}

/**
 * Represents a recently used aliment entry.
 */
data class RecentEntry(
    val aliment: Aliment,
    val quantiteGrammes: Double,
    val mealType: MealType,
)

/**
 * Represents a recipe search result for JOURNAL-02 (recette mode).
 */
data class RecetteSearchResult(
    val id: String,
    val nom: String,
    val tempsPreparationMin: Int,
)
