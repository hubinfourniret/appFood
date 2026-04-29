package com.appfood.shared.ui.recommandation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.request.AddJournalEntryRequest
import com.appfood.shared.data.repository.JournalRepository
import com.appfood.shared.data.repository.RecommandationRepository
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the recommendations screen (RECO-01).
 * Displays food suggestions to fill nutriment deficits.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class RecommandationViewModel(
    private val recommandationRepository: RecommandationRepository,
    private val journalRepository: JournalRepository,
) : ViewModel() {

    private val currentUserId: String = "me"
    private val currentDate: String get() = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    // --- Onglet actif ---
    private val _selectedTab = MutableStateFlow(RecommandationTab.ALIMENTS)
    val selectedTab: StateFlow<RecommandationTab> = _selectedTab.asStateFlow()

    // --- RECO-01 : Recommandations aliments ---
    private val _state = MutableStateFlow<RecommandationState>(RecommandationState.Loading)
    val state: StateFlow<RecommandationState> = _state.asStateFlow()

    // --- RECO-02 : Recommandations recettes ---
    private val _recetteState = MutableStateFlow<RecommandationRecetteState>(RecommandationRecetteState.Loading)
    val recetteState: StateFlow<RecommandationRecetteState> = _recetteState.asStateFlow()

    // --- RECO-03 : Portions selectionnees par recette ---
    private val _recettePortions = MutableStateFlow<Map<String, Int>>(emptyMap())
    val recettePortions: StateFlow<Map<String, Int>> = _recettePortions.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun onTabSelected(tab: RecommandationTab) {
        _selectedTab.value = tab
    }

    fun loadRecommandations() {
        loadAlimentRecommandations()
        loadRecetteRecommandations()
    }

    private fun loadAlimentRecommandations() {
        _state.value = RecommandationState.Loading
        viewModelScope.launch {
            when (val result = recommandationRepository.getAlimentRecommandations(
                userId = currentUserId,
                date = currentDate,
            )) {
                is AppResult.Success -> {
                    val data = result.data
                    _state.value = if (data.isEmpty()) {
                        RecommandationState.NoDeficit
                    } else {
                        RecommandationState.Success(data.map { reco ->
                            RecommandationUiModel(
                                alimentId = reco.aliment.id,
                                alimentNom = reco.aliment.nom,
                                quantiteSuggereGrammes = reco.quantiteSuggereGrammes,
                                couverture = reco.pourcentageCouverture.map { (type, pct) ->
                                    CouvertureNutriment(
                                        nutrimentType = type,
                                        nutrimentLabel = type.name,
                                        pourcentage = pct,
                                    )
                                },
                            )
                        })
                    }
                }
                is AppResult.Error -> {
                    _state.value = RecommandationState.Error(result.message)
                }
            }
        }
    }

    private fun loadRecetteRecommandations() {
        _recetteState.value = RecommandationRecetteState.Loading
        viewModelScope.launch {
            when (val result = recommandationRepository.getRecetteRecommandations(
                userId = currentUserId,
                date = currentDate,
            )) {
                is AppResult.Success -> {
                    val data = result.data
                    _recetteState.value = if (data.isEmpty()) {
                        RecommandationRecetteState.NoDeficit
                    } else {
                        RecommandationRecetteState.Success(data.map { reco ->
                            RecommandationRecetteUiModel(
                                recetteId = reco.recette.id,
                                recetteNom = reco.recette.nom,
                                tempsPreparationMin = reco.recette.tempsPreparationMin,
                                pourcentageCouvertureGlobal = reco.pourcentageCouvertureGlobal.toInt(),
                                nutrimentsCibles = reco.nutrimentsCibles.map { it.name },
                            )
                        })
                    }
                }
                is AppResult.Error -> {
                    _recetteState.value = RecommandationRecetteState.Error(result.message)
                }
            }
        }
    }

    fun onAteThis(recommandation: RecommandationUiModel) {
        viewModelScope.launch {
            val request = AddJournalEntryRequest(
                date = currentDate,
                mealType = MealType.COLLATION.name,
                alimentId = recommandation.alimentId,
                nom = recommandation.alimentNom,
                quantiteGrammes = recommandation.quantiteSuggereGrammes,
            )
            when (val result = journalRepository.addEntry(request)) {
                is AppResult.Success -> {
                    _successMessage.value = com.appfood.shared.ui.Strings.recoAlimentAddedToJournal(recommandation.alimentNom)
                }
                is AppResult.Error -> {
                    // Silently fail for now — user can retry
                }
            }
            loadRecommandations()
        }
    }

    fun onRecettePortionsChanged(recetteId: String, portions: Int) {
        if (portions < 1) return
        _recettePortions.value = _recettePortions.value + (recetteId to portions)
    }

    fun getPortionsForRecette(recetteId: String): Int {
        return _recettePortions.value[recetteId] ?: 1
    }

    fun onAddRecetteToJournal(recette: RecommandationRecetteUiModel) {
        val portions = getPortionsForRecette(recette.recetteId)
        viewModelScope.launch {
            val request = AddJournalEntryRequest(
                date = currentDate,
                mealType = MealType.COLLATION.name,
                recetteId = recette.recetteId,
                nbPortions = portions.toDouble(),
            )
            when (val result = journalRepository.addEntry(request)) {
                is AppResult.Success -> {
                    _successMessage.value = com.appfood.shared.ui.Strings.RECO_ADDED_TO_JOURNAL_SUCCESS
                }
                is AppResult.Error -> {
                    // Silently fail for now — user can retry
                }
            }
            loadRecommandations()
        }
    }

    fun retry() {
        loadRecommandations()
    }
}

// --- States ---

sealed interface RecommandationState {
    data object Loading : RecommandationState
    data class Success(val recommandations: List<RecommandationUiModel>) : RecommandationState
    data object NoDeficit : RecommandationState
    data class Error(val message: String) : RecommandationState
}

data class RecommandationUiModel(
    val alimentId: String,
    val alimentNom: String,
    val quantiteSuggereGrammes: Double,
    val couverture: List<CouvertureNutriment>,
)

data class CouvertureNutriment(
    val nutrimentType: NutrimentType,
    val nutrimentLabel: String,
    val pourcentage: Double,
)

// --- Onglet ---

enum class RecommandationTab {
    ALIMENTS, RECETTES
}

// --- RECO-02 : Recommandations recettes ---

sealed interface RecommandationRecetteState {
    data object Loading : RecommandationRecetteState
    data class Success(val recettes: List<RecommandationRecetteUiModel>) : RecommandationRecetteState
    data object NoDeficit : RecommandationRecetteState
    data class Error(val message: String) : RecommandationRecetteState
}

data class RecommandationRecetteUiModel(
    val recetteId: String,
    val recetteNom: String,
    val tempsPreparationMin: Int,
    val pourcentageCouvertureGlobal: Int,
    val nutrimentsCibles: List<String>,
)
