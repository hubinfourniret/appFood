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
) : ViewModel() {

    private val _state = MutableStateFlow<RecommandationState>(RecommandationState.Loading)
    val state: StateFlow<RecommandationState> = _state.asStateFlow()

    fun loadRecommandations() {
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

    fun onAteThis(recommandation: RecommandationUiModel) {
        viewModelScope.launch {
            // TODO: Call ajouterEntreeUseCase when created by SHARED agent
            // This would add the suggested food to the user's journal
            // Then reload recommendations

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
