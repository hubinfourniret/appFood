package com.appfood.shared.ui.recommandation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.model.NutrimentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the recommendations screen (RECO-01).
 * Displays food suggestions to fill nutriment deficits.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class RecommandationViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val getRecommandationsUseCase: GetRecommandationsUseCase,
    // private val ajouterEntreeUseCase: AjouterEntreeUseCase,
    // private val recommandationRecetteUseCase: RecommandationRecetteUseCase,
) : ViewModel() {

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
            // TODO: Call getRecommandationsUseCase when created by SHARED agent
            // val result = getRecommandationsUseCase()
            // when (result) {
            //     is AppResult.Success -> {
            //         _state.value = if (result.data.isEmpty()) {
            //             RecommandationState.NoDeficit
            //         } else {
            //             RecommandationState.Success(result.data)
            //         }
            //     }
            //     is AppResult.Error -> {
            //         _state.value = RecommandationState.Error(result.message)
            //     }
            // }

            // Stub: simulate no deficit (empty list)
            _state.value = RecommandationState.NoDeficit
        }
    }

    private fun loadRecetteRecommandations() {
        _recetteState.value = RecommandationRecetteState.Loading
        viewModelScope.launch {
            // TODO: Call recommandationRecetteUseCase when created by SHARED agent
            // val result = recommandationRecetteUseCase.execute(quotaStatuses, recettes, regime)
            // when (result) {
            //     is AppResult.Success -> {
            //         _recetteState.value = if (result.data.isEmpty()) {
            //             RecommandationRecetteState.NoDeficit
            //         } else {
            //             RecommandationRecetteState.Success(
            //                 result.data.map { it.toUiModel() }
            //             )
            //         }
            //     }
            //     is AppResult.Error -> {
            //         _recetteState.value = RecommandationRecetteState.Error(result.message)
            //     }
            // }

            // Stub: simulate no deficit (empty list)
            _recetteState.value = RecommandationRecetteState.NoDeficit
        }
    }

    fun onAteThis(recommandation: RecommandationUiModel) {
        viewModelScope.launch {
            // TODO: Call ajouterEntreeUseCase when created by SHARED agent
            // This would add the suggested food to the user's journal
            // Then reload recommendations

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
            // TODO: Call ajouterRecetteAuJournalUseCase when created by SHARED agent
            // 1. Creer une entree journal avec les nutriments calcules proportionnellement aux portions
            //    val nutriments = recette.nutrimentsTotaux * portions
            //    ajouterEntreeJournalUseCase(recetteId = recette.recetteId, portions = portions, nutriments = nutriments)
            // 2. Afficher un feedback de succes
            // 3. Recharger les recommandations

            _successMessage.value = com.appfood.shared.ui.Strings.RECO_ADDED_TO_JOURNAL_SUCCESS
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
