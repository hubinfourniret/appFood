package com.appfood.shared.ui.recette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.model.MealType
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RegimeAlimentaire
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for the recipe book screen (RECETTES-01).
 * Manages search, filters, sorting and pagination.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class RecettesViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val rechercherRecettesUseCase: RechercherRecettesUseCase,
    // private val getRecettesUseCase: GetRecettesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<RecettesState>(RecettesState.Loading)
    val state: StateFlow<RecettesState> = _state.asStateFlow()

    // --- Search ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Filters ---
    private val _selectedRegimes = MutableStateFlow<Set<RegimeAlimentaire>>(emptySet())
    val selectedRegimes: StateFlow<Set<RegimeAlimentaire>> = _selectedRegimes.asStateFlow()

    private val _selectedMealTypes = MutableStateFlow<Set<MealType>>(emptySet())
    val selectedMealTypes: StateFlow<Set<MealType>> = _selectedMealTypes.asStateFlow()

    private val _maxTempsPrepMin = MutableStateFlow<Int?>(null)
    val maxTempsPrepMin: StateFlow<Int?> = _maxTempsPrepMin.asStateFlow()

    // --- Sort ---
    private val _sortOption = MutableStateFlow(RecetteSortOption.PERTINENCE)
    val sortOption: StateFlow<RecetteSortOption> = _sortOption.asStateFlow()

    // --- Pagination ---
    private val _currentPage = MutableStateFlow(0)
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    @OptIn(FlowPreview::class)
    fun init() {
        // Debounce search queries
        _searchQuery
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { loadRecettes(resetPage = true) }
            .launchIn(viewModelScope)

        // React to filter/sort changes
        combine(
            _selectedRegimes,
            _selectedMealTypes,
            _maxTempsPrepMin,
            _sortOption,
        ) { _, _, _, _ -> }
            .onEach { loadRecettes(resetPage = true) }
            .launchIn(viewModelScope)

        loadRecettes(resetPage = true)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onRegimeToggle(regime: RegimeAlimentaire) {
        _selectedRegimes.value = _selectedRegimes.value.let { current ->
            if (regime in current) current - regime else current + regime
        }
    }

    fun onMealTypeToggle(mealType: MealType) {
        _selectedMealTypes.value = _selectedMealTypes.value.let { current ->
            if (mealType in current) current - mealType else current + mealType
        }
    }

    fun onMaxTempsPrepChanged(minutes: Int?) {
        _maxTempsPrepMin.value = minutes
    }

    fun onSortChanged(option: RecetteSortOption) {
        _sortOption.value = option
    }

    fun onClearFilters() {
        _selectedRegimes.value = emptySet()
        _selectedMealTypes.value = emptySet()
        _maxTempsPrepMin.value = null
        _sortOption.value = RecetteSortOption.PERTINENCE
    }

    fun loadMore() {
        if (!_hasMore.value) return
        _currentPage.value++
        loadRecettes(resetPage = false)
    }

    fun retry() {
        loadRecettes(resetPage = true)
    }

    private fun loadRecettes(resetPage: Boolean) {
        if (resetPage) {
            _currentPage.value = 0
            _hasMore.value = true
        }

        val currentState = _state.value
        if (resetPage) {
            _state.value = RecettesState.Loading
        }

        viewModelScope.launch {
            // TODO: Call use cases when created by SHARED agent
            // val result = rechercherRecettesUseCase(
            //     query = _searchQuery.value,
            //     regimes = _selectedRegimes.value.toList(),
            //     mealTypes = _selectedMealTypes.value.toList(),
            //     maxTempsPrepMin = _maxTempsPrepMin.value,
            //     sort = _sortOption.value,
            //     page = _currentPage.value,
            //     pageSize = PAGE_SIZE,
            // )
            // when (result) {
            //     is AppResult.Success -> {
            //         val newRecettes = result.data
            //         _hasMore.value = newRecettes.size >= PAGE_SIZE
            //         val allRecettes = if (resetPage) newRecettes
            //             else (currentState as? RecettesState.Success)?.recettes.orEmpty() + newRecettes
            //         _state.value = RecettesState.Success(recettes = allRecettes)
            //     }
            //     is AppResult.Error -> {
            //         _state.value = RecettesState.Error(result.message)
            //     }
            // }

            // Stub: empty list
            _state.value = RecettesState.Success(recettes = emptyList())
            _hasMore.value = false
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val PAGE_SIZE = 20
    }
}

// --- States ---

sealed interface RecettesState {
    data object Loading : RecettesState
    data class Success(val recettes: List<Recette>) : RecettesState
    data class Error(val message: String) : RecettesState
}

enum class RecetteSortOption {
    PERTINENCE, POPULARITE, TEMPS_PREPARATION
}
