package com.appfood.shared.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.api.request.UpdateJournalEntryRequest
import com.appfood.shared.api.response.AlimentResponse
import com.appfood.shared.api.response.NutrimentValuesResponse
import com.appfood.shared.data.remote.PortionApi
import com.appfood.shared.data.repository.AlimentRepository
import com.appfood.shared.data.repository.JournalRepository
import com.appfood.shared.data.repository.RecetteRepository
import com.appfood.shared.model.Aliment
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentValues
import com.appfood.shared.model.PortionStandard
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.SourceAliment
import com.appfood.shared.sync.SyncEnqueuer
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json

/**
 * ViewModel for the entire journal feature (JOURNAL-01, JOURNAL-03, JOURNAL-04, JOURNAL-06, PORTIONS-01).
 * Manages food search, entry creation, favorites, recents, edit and delete.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val alimentRepository: AlimentRepository,
    private val recetteRepository: RecetteRepository,
    private val syncManager: SyncEnqueuer,
    private val portionApi: PortionApi? = null,
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

    // --- Loaded portions (UX-07 — from API, matched by aliment name/category) ---
    private val _loadedPortions = MutableStateFlow<List<PortionStandard>>(emptyList())
    val loadedPortions: StateFlow<List<PortionStandard>> = _loadedPortions.asStateFlow()

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

    // --- Delete confirmation state (JOURNAL-06) ---
    private val _showDeleteConfirmation = MutableStateFlow<String?>(null)
    val showDeleteConfirmation: StateFlow<String?> = _showDeleteConfirmation.asStateFlow()

    // --- Nutritional preview for current selection (reactive) ---
    val nutritionPreview: StateFlow<NutrimentValues?> = combine(
        _selectedAliment,
        _quantityGrams,
    ) { aliment, grams ->
        if (aliment == null) return@combine null
        val factor = grams / 100.0
        val n = aliment.nutrimentsPour100g
        NutrimentValues(
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
            when (val result = alimentRepository.search(query)) {
                is AppResult.Success -> {
                    val aliments = result.data.data.map { it.toDomain() }
                    _searchState.value = if (aliments.isEmpty()) {
                        SearchState.Empty
                    } else {
                        SearchState.Results(aliments)
                    }
                }
                is AppResult.Error -> {
                    _searchState.value = SearchState.Error(result.message)
                }
            }
        }
    }

    // --- Aliment selection ---

    fun onAlimentSelected(aliment: Aliment) {
        _selectedAliment.value = aliment
        _quantityGrams.value = DEFAULT_QUANTITY
        _selectedPortion.value = null
        _loadedPortions.value = emptyList()
        _addEntryState.value = AddEntryState.SelectPortion
        // Charger les portions pertinentes depuis l'API (UX-07)
        loadPortionsForAliment(aliment)
    }

    private fun loadPortionsForAliment(aliment: Aliment) {
        val api = portionApi ?: return
        viewModelScope.launch {
            try {
                val response = api.getPortions(
                    alimentId = aliment.id,
                    alimentNom = aliment.nom,
                )
                _loadedPortions.value = response.data.map { portion ->
                    PortionStandard(
                        id = portion.id,
                        alimentId = portion.alimentId,
                        nom = portion.nom,
                        quantiteGrammes = portion.quantiteGrammes,
                        estGenerique = portion.estGenerique,
                        estPersonnalise = portion.estPersonnalise,
                        userId = null,
                    )
                }
            } catch (_: Exception) {
                // Fallback : pas de portions chargees, l'utilisateur utilise le champ grammes
            }
        }
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
        val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())

        _addEntryState.value = AddEntryState.Saving

        val request = AddJournalEntryRequest(
            date = today.toString(),
            mealType = mealType.name,
            alimentId = aliment.id,
            nom = aliment.nom,
            quantiteGrammes = grams,
        )

        viewModelScope.launch {
            when (val result = journalRepository.addEntry(request)) {
                is AppResult.Success -> {
                    _addEntryState.value = AddEntryState.Saved
                }
                is AppResult.Error -> {
                    // API failed — enqueue for offline sync so the entry
                    // is persisted locally and will be synced later.
                    // API failed — fallback to offline sync
                    val payloadJson = Json.encodeToString(
                        AddJournalEntryRequest.serializer(),
                        request,
                    )
                    syncManager.enqueue(
                        entityType = "journal",
                        entityId = "${aliment.id}_${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                        action = "CREATE",
                        payloadJson = payloadJson,
                    )
                    _addEntryState.value = AddEntryState.SavedOffline
                }
            }
        }
    }

    // --- Favorites (JOURNAL-03) ---

    fun onToggleFavorite(aliment: Aliment) {
        viewModelScope.launch {
            val isFav = _favorites.value.any { it.id == aliment.id }
            // Optimistic local update
            _favorites.update { current ->
                if (isFav) current.filter { it.id != aliment.id } else current + aliment
            }
            // Persist to backend
            val result = if (isFav) {
                journalRepository.removeFavori(aliment.id)
            } else {
                journalRepository.addFavori(aliment.id)
            }
            if (result is AppResult.Error) {
                // Revert on failure
                _favorites.update { current ->
                    if (isFav) current + aliment else current.filter { it.id != aliment.id }
                }
            }
        }
    }

    fun isFavorite(alimentId: String): Boolean {
        return _favorites.value.any { it.id == alimentId }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            when (val result = journalRepository.getFavoris()) {
                is AppResult.Success -> {
                    // Convertir les JournalEntryResponse en Aliment pour l'affichage
                    // On charge les details de chaque aliment favori
                    val alimentIds = result.data.data.mapNotNull { it.alimentId }.distinct()
                    val aliments = alimentIds.mapNotNull { id ->
                        when (val alimentResult = alimentRepository.getById(id)) {
                            is AppResult.Success -> alimentResult.data.toDomain()
                            is AppResult.Error -> null
                        }
                    }
                    _favorites.value = aliments
                }
                is AppResult.Error -> { /* echec silencieux, donnees offline */ }
            }
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
            when (val result = journalRepository.getRecents(limit = MAX_RECENTS)) {
                is AppResult.Success -> {
                    val entries = result.data.data.mapNotNull { entry ->
                        val alimentId = entry.alimentId ?: return@mapNotNull null
                        val alimentResult = alimentRepository.getById(alimentId)
                        val aliment = when (alimentResult) {
                            is AppResult.Success -> alimentResult.data.toDomain()
                            is AppResult.Error -> return@mapNotNull null
                        }
                        val mealType = runCatching { MealType.valueOf(entry.mealType) }.getOrNull()
                            ?: return@mapNotNull null
                        RecentEntry(
                            aliment = aliment,
                            quantiteGrammes = entry.quantiteGrammes,
                            mealType = mealType,
                        )
                    }
                    _recents.value = entries
                }
                is AppResult.Error -> { /* echec silencieux */ }
            }
        }
    }

    // --- Edit / Delete (JOURNAL-06) ---

    fun onEditEntry(entryId: String, newQuantityGrams: Double) {
        _editState.value = EditEntryState.Saving
        viewModelScope.launch {
            val request = UpdateJournalEntryRequest(
                quantiteGrammes = newQuantityGrams,
            )
            when (val result = journalRepository.updateEntry(entryId, request)) {
                is AppResult.Success -> {
                    _editState.value = EditEntryState.Success
                }
                is AppResult.Error -> {
                    _editState.value = EditEntryState.Error(result.message)
                }
            }
        }
    }

    fun onDeleteEntry(entryId: String) {
        _editState.value = EditEntryState.Saving
        viewModelScope.launch {
            when (val result = journalRepository.deleteEntry(entryId)) {
                is AppResult.Success -> {
                    _editState.value = EditEntryState.Deleted
                }
                is AppResult.Error -> {
                    _editState.value = EditEntryState.Error(result.message)
                }
            }
        }
    }

    /** Show confirmation dialog before deleting (JOURNAL-06). */
    fun onRequestDelete(entryId: String) {
        _showDeleteConfirmation.value = entryId
    }

    /** User confirmed deletion — proceed. */
    fun onConfirmDelete() {
        val entryId = _showDeleteConfirmation.value ?: return
        _showDeleteConfirmation.value = null
        onDeleteEntry(entryId)
    }

    /** User cancelled deletion. */
    fun onCancelDelete() {
        _showDeleteConfirmation.value = null
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
            when (val result = recetteRepository.listRecettes(query = query)) {
                is AppResult.Success -> {
                    _recetteSearchResults.value = result.data.map {
                        RecetteSearchResult(
                            id = it.id,
                            nom = it.nom,
                            tempsPreparationMin = it.tempsPreparationMin,
                        )
                    }
                }
                is AppResult.Error -> {
                    _recetteSearchResults.value = emptyList()
                }
            }
        }
    }

    fun onRecetteSelected(recetteId: String) {
        viewModelScope.launch {
            // Ajout direct de 1 portion au journal (navigation vers detail recette = V1.1 UX-06)
            val mealType = _selectedMealType.value ?: return@launch
            val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
            val request = AddJournalEntryRequest(
                date = today.toString(),
                mealType = mealType.name,
                recetteId = recetteId,
                nbPortions = 1.0,
            )
            when (val result = journalRepository.addEntry(request)) {
                is AppResult.Success -> {
                    _addEntryState.value = AddEntryState.Saved
                }
                is AppResult.Error -> {
                    _addEntryState.value = AddEntryState.Error(result.message)
                }
            }
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

// --- Mapping helpers ---

/**
 * Convertit une AlimentResponse (API) en Aliment (domain).
 */
private fun AlimentResponse.toDomain(): Aliment = Aliment(
    id = id,
    nom = nom,
    marque = marque,
    source = runCatching { SourceAliment.valueOf(source) }.getOrDefault(SourceAliment.CIQUAL),
    sourceId = sourceId,
    codeBarres = codeBarres,
    categorie = categorie,
    regimesCompatibles = regimesCompatibles.mapNotNull {
        runCatching { RegimeAlimentaire.valueOf(it) }.getOrNull()
    },
    nutrimentsPour100g = nutrimentsPour100g.toDomain(),
    portionsStandard = portionsStandard.map { portion ->
        PortionStandard(
            id = portion.id,
            alimentId = portion.alimentId,
            nom = portion.nom,
            quantiteGrammes = portion.quantiteGrammes,
            estGenerique = portion.estGenerique,
            estPersonnalise = portion.estPersonnalise,
            userId = null,
        )
    },
)

private fun NutrimentValuesResponse.toDomain(): NutrimentValues = NutrimentValues(
    calories = calories,
    proteines = proteines,
    glucides = glucides,
    lipides = lipides,
    fibres = fibres,
    sel = sel,
    sucres = sucres,
    fer = fer,
    calcium = calcium,
    zinc = zinc,
    magnesium = magnesium,
    vitamineB12 = vitamineB12,
    vitamineD = vitamineD,
    vitamineC = vitamineC,
    omega3 = omega3,
    omega6 = omega6,
)

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
    /** Entry saved locally in sync_queue, will be synced when connection is restored. */
    data object SavedOffline : AddEntryState
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
