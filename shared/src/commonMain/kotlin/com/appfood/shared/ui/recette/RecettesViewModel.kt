package com.appfood.shared.ui.recette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.model.IngredientRecette
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.Recette
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.CreateRecetteRequest
import com.appfood.shared.api.request.IngredientRequest
import com.appfood.shared.data.repository.JournalRepository
import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.sync.SyncEnqueuer
import com.appfood.shared.util.AppResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the recipe feature (RECETTES-01, RECETTES-02, RECETTES-03).
 * Manages search, filters, sorting, pagination, detail, and admin creation.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class RecettesViewModel(
    private val recetteRepository: RecetteRepository,
    private val journalRepository: JournalRepository,
    private val syncEnqueuer: SyncEnqueuer,
) : ViewModel() {

    // ==================== LIST STATE (RECETTES-01) ====================

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

    // ==================== DETAIL STATE (RECETTES-02) ====================

    private val _detailState = MutableStateFlow<RecetteDetailState>(RecetteDetailState.Loading)
    val detailState: StateFlow<RecetteDetailState> = _detailState.asStateFlow()

    private val _selectedPortions = MutableStateFlow(1)
    val selectedPortions: StateFlow<Int> = _selectedPortions.asStateFlow()

    private val _isDetailFavorite = MutableStateFlow(false)
    val isDetailFavorite: StateFlow<Boolean> = _isDetailFavorite.asStateFlow()

    // --- Meal selection dialog for adding recipe to journal ---
    private val _showMealSelectionDialog = MutableStateFlow(false)
    val showMealSelectionDialog: StateFlow<Boolean> = _showMealSelectionDialog.asStateFlow()

    private val _addToJournalState = MutableStateFlow<AddRecetteToJournalState>(AddRecetteToJournalState.Idle)
    val addToJournalState: StateFlow<AddRecetteToJournalState> = _addToJournalState.asStateFlow()

    // ==================== CREATE STATE (RECETTES-03) ====================

    private val _createRecetteState = MutableStateFlow<CreateRecetteState>(CreateRecetteState.Idle)
    val createRecetteState: StateFlow<CreateRecetteState> = _createRecetteState.asStateFlow()

    private val _createRecetteForm = MutableStateFlow(CreateRecetteFormState())
    val createRecetteForm: StateFlow<CreateRecetteFormState> = _createRecetteForm.asStateFlow()

    // ==================== LIST ACTIONS ====================

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
            val regimeFilter = _selectedRegimes.value.firstOrNull()?.name
            val mealTypeFilter = _selectedMealTypes.value.firstOrNull()?.name
            val sortStr = when (_sortOption.value) {
                RecetteSortOption.PERTINENCE -> null
                RecetteSortOption.POPULARITE -> "popularite"
                RecetteSortOption.TEMPS_PREPARATION -> "temps_preparation"
            }
            val queryStr = _searchQuery.value.ifBlank { null }

            when (val result = recetteRepository.listRecettes(
                regime = regimeFilter,
                typeRepas = mealTypeFilter,
                sort = sortStr,
                query = queryStr,
                page = _currentPage.value + 1,
                limit = PAGE_SIZE,
            )) {
                is AppResult.Success -> {
                    val newRecettes = result.data
                    val existingRecettes = if (resetPage) {
                        emptyList()
                    } else {
                        (currentState as? RecettesState.Success)?.recettes.orEmpty()
                    }
                    _state.value = RecettesState.Success(recettes = existingRecettes + newRecettes)
                    _hasMore.value = newRecettes.size >= PAGE_SIZE
                }
                is AppResult.Error -> {
                    _state.value = RecettesState.Error(result.message)
                }
            }
        }
    }

    // ==================== DETAIL ACTIONS (RECETTES-02) ====================

    fun loadRecetteDetail(id: String) {
        _detailState.value = RecetteDetailState.Loading
        viewModelScope.launch {
            when (val result = recetteRepository.getRecette(id)) {
                is AppResult.Success -> {
                    val r = result.data
                    _selectedPortions.value = r.nbPortions
                    _detailState.value = RecetteDetailState.Success(
                        id = r.id,
                        nom = r.nom,
                        description = r.description,
                        tempsPreparationMin = r.tempsPreparationMin,
                        tempsCuissonMin = r.tempsCuissonMin,
                        nbPortions = r.nbPortions,
                        ingredients = r.ingredients,
                        etapes = r.etapes,
                        nutrimentsTotaux = r.nutrimentsTotaux,
                        imageUrl = r.imageUrl,
                    )
                }
                is AppResult.Error -> {
                    _detailState.value = RecetteDetailState.Error(result.message)
                }
            }
        }
    }

    fun onDetailPortionsChanged(portions: Int) {
        if (portions >= 1) {
            _selectedPortions.value = portions
        }
    }

    fun onToggleDetailFavorite() {
        _isDetailFavorite.value = !_isDetailFavorite.value
        viewModelScope.launch {
            // TODO: Call toggleFavoriRecetteUseCase when created by SHARED agent
        }
    }

    fun onAddRecetteToJournal() {
        _showMealSelectionDialog.value = true
    }

    fun onDismissMealSelectionDialog() {
        _showMealSelectionDialog.value = false
    }

    fun onMealSelectedForRecette(mealType: MealType) {
        _showMealSelectionDialog.value = false
        val detail = _detailState.value as? RecetteDetailState.Success ?: return
        val portions = _selectedPortions.value

        _addToJournalState.value = AddRecetteToJournalState.Saving
        val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
        val request = AddJournalEntryRequest(
            date = today.toString(),
            mealType = mealType.name,
            recetteId = detail.id,
            nom = detail.nom,
            nbPortions = portions.toDouble(),
        )

        viewModelScope.launch {
            when (val result = journalRepository.addEntry(request)) {
                is AppResult.Success -> {
                    _addToJournalState.value = AddRecetteToJournalState.Success
                }
                is AppResult.Error -> {
                    // Offline fallback — enqueue for sync
                    val payloadJson = Json.encodeToString(
                        AddJournalEntryRequest.serializer(),
                        request,
                    )
                    syncEnqueuer.enqueue(
                        entityType = "journal",
                        entityId = "${detail.id}_${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                        action = "CREATE",
                        payloadJson = payloadJson,
                    )
                    _addToJournalState.value = AddRecetteToJournalState.SavedOffline
                }
            }
        }
    }

    fun resetAddToJournalState() {
        _addToJournalState.value = AddRecetteToJournalState.Idle
    }

    // ==================== CREATE ACTIONS (RECETTES-03) ====================

    fun onCreateNomChanged(value: String) {
        _createRecetteForm.update { it.copy(nom = value) }
    }

    fun onCreateDescriptionChanged(value: String) {
        _createRecetteForm.update { it.copy(description = value) }
    }

    fun onCreateImageUrlChanged(value: String) {
        _createRecetteForm.update { it.copy(imageUrl = value) }
    }

    fun onCreateTempsPrepChanged(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _createRecetteForm.update { it.copy(tempsPrepMin = value) }
        }
    }

    fun onCreateTempsCuissonChanged(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _createRecetteForm.update { it.copy(tempsCuissonMin = value) }
        }
    }

    fun onCreateNbPortionsChanged(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _createRecetteForm.update { it.copy(nbPortions = value) }
        }
    }

    fun onCreateRegimeChanged(regime: RegimeAlimentaire) {
        _createRecetteForm.update { it.copy(regime = regime) }
    }

    fun onCreateAddIngredient() {
        _createRecetteForm.update {
            it.copy(ingredients = it.ingredients + IngredientFormEntry())
        }
    }

    fun onCreateRemoveIngredient(index: Int) {
        _createRecetteForm.update {
            it.copy(ingredients = it.ingredients.toMutableList().also { list -> list.removeAt(index) })
        }
    }

    fun onCreateIngredientNameChanged(index: Int, name: String) {
        _createRecetteForm.update { form ->
            form.copy(
                ingredients = form.ingredients.toMutableList().also { list ->
                    list[index] = list[index].copy(alimentNom = name)
                },
            )
        }
    }

    fun onCreateIngredientQuantityChanged(index: Int, quantity: String) {
        if (quantity.isEmpty() || quantity.matches(Regex("^\\d*\\.?\\d*$"))) {
            _createRecetteForm.update { form ->
                form.copy(
                    ingredients = form.ingredients.toMutableList().also { list ->
                        list[index] = list[index].copy(quantiteGrammes = quantity)
                    },
                )
            }
        }
    }

    fun onCreateAddEtape() {
        _createRecetteForm.update {
            it.copy(etapes = it.etapes + "")
        }
    }

    fun onCreateRemoveEtape(index: Int) {
        _createRecetteForm.update {
            it.copy(etapes = it.etapes.toMutableList().also { list -> list.removeAt(index) })
        }
    }

    fun onCreateEtapeChanged(index: Int, text: String) {
        _createRecetteForm.update { form ->
            form.copy(
                etapes = form.etapes.toMutableList().also { list -> list[index] = text },
            )
        }
    }

    fun onCreateMoveEtapeUp(index: Int) {
        if (index <= 0) return
        _createRecetteForm.update { form ->
            form.copy(
                etapes = form.etapes.toMutableList().also { list ->
                    val item = list.removeAt(index)
                    list.add(index - 1, item)
                },
            )
        }
    }

    fun onCreateMoveEtapeDown(index: Int) {
        _createRecetteForm.update { form ->
            if (index >= form.etapes.lastIndex) return@update form
            form.copy(
                etapes = form.etapes.toMutableList().also { list ->
                    val item = list.removeAt(index)
                    list.add(index + 1, item)
                },
            )
        }
    }

    fun onCreateIngredientIdChanged(index: Int, id: String) {
        _createRecetteForm.update { form ->
            form.copy(
                ingredients = form.ingredients.toMutableList().also { list ->
                    list[index] = list[index].copy(alimentId = id)
                },
            )
        }
    }

    fun onCreateRecetteSubmit() {
        val form = _createRecetteForm.value
        if (form.nom.isBlank()) return

        _createRecetteState.value = CreateRecetteState.Saving
        viewModelScope.launch {
            val request = CreateRecetteRequest(
                nom = form.nom,
                description = form.description,
                tempsPreparationMin = form.tempsPrepMin.toIntOrNull() ?: 0,
                tempsCuissonMin = form.tempsCuissonMin.toIntOrNull() ?: 0,
                nbPortions = form.nbPortions.toIntOrNull() ?: 1,
                regimesCompatibles = listOf(form.regime.name),
                typeRepas = emptyList(),
                ingredients = form.ingredients.mapNotNull { entry ->
                    val qty = entry.quantiteGrammes.toDoubleOrNull() ?: return@mapNotNull null
                    val id = entry.alimentId.ifBlank { return@mapNotNull null }
                    IngredientRequest(alimentId = id, quantiteGrammes = qty)
                },
                etapes = form.etapes.filter { it.isNotBlank() },
                imageUrl = form.imageUrl.ifBlank { null },
                publie = true,
            )
            when (val result = recetteRepository.createRecette(request)) {
                is AppResult.Success -> {
                    _createRecetteState.value = CreateRecetteState.Success
                    _createRecetteForm.value = CreateRecetteFormState()
                }
                is AppResult.Error -> {
                    _createRecetteState.value = CreateRecetteState.Error(result.message)
                }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val PAGE_SIZE = 20
    }
}

// ==================== LIST STATES ====================

sealed interface RecettesState {
    data object Loading : RecettesState
    data class Success(val recettes: List<Recette>) : RecettesState
    data class Error(val message: String) : RecettesState
}

enum class RecetteSortOption {
    PERTINENCE, POPULARITE, TEMPS_PREPARATION
}

// ==================== DETAIL STATES (RECETTES-02) ====================

sealed interface RecetteDetailState {
    data object Loading : RecetteDetailState
    data class Success(
        val id: String,
        val nom: String,
        val description: String,
        val tempsPreparationMin: Int,
        val tempsCuissonMin: Int,
        val nbPortions: Int,
        val ingredients: List<IngredientRecette>,
        val etapes: List<String>,
        val nutrimentsTotaux: NutrimentValues,
        val imageUrl: String?,
    ) : RecetteDetailState
    data class Error(val message: String) : RecetteDetailState
}

// ==================== CREATE STATES (RECETTES-03) ====================

sealed interface CreateRecetteState {
    data object Idle : CreateRecetteState
    data object Saving : CreateRecetteState
    data object Success : CreateRecetteState
    data class Error(val message: String) : CreateRecetteState
}

data class CreateRecetteFormState(
    val nom: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val tempsPrepMin: String = "",
    val tempsCuissonMin: String = "",
    val nbPortions: String = "4",
    val regime: RegimeAlimentaire = RegimeAlimentaire.VEGAN,
    val ingredients: List<IngredientFormEntry> = emptyList(),
    val etapes: List<String> = emptyList(),
)

data class IngredientFormEntry(
    val alimentId: String = "",
    val alimentNom: String = "",
    val quantiteGrammes: String = "",
)

// ==================== ADD RECETTE TO JOURNAL STATE ====================

sealed interface AddRecetteToJournalState {
    data object Idle : AddRecetteToJournalState
    data object Saving : AddRecetteToJournalState
    data object Success : AddRecetteToJournalState
    data object SavedOffline : AddRecetteToJournalState
}
