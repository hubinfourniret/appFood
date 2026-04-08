package com.appfood.shared.ui.quota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.request.UpdateQuotaRequest
import com.appfood.shared.data.local.LocalUserDataSource
import com.appfood.shared.data.repository.QuotaRepository
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.model.QuotaJournalier
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the quota management screen (QUOTAS-02).
 * Allows users to view and customize their daily nutriment quotas.
 */
class QuotaViewModel(
    private val quotaRepository: QuotaRepository,
    private val localUserDataSource: LocalUserDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow<QuotaManagementState>(QuotaManagementState.Loading)
    val state: StateFlow<QuotaManagementState> = _state.asStateFlow()

    private val _editingNutriment = MutableStateFlow<NutrimentType?>(null)
    val editingNutriment: StateFlow<NutrimentType?> = _editingNutriment.asStateFlow()

    private val _editValue = MutableStateFlow("")
    val editValue: StateFlow<String> = _editValue.asStateFlow()

    init {
        loadQuotas()
    }

    private fun getCurrentUserId(): String? {
        return localUserDataSource.findAll().firstOrNull()?.id
    }

    fun loadQuotas() {
        _state.value = QuotaManagementState.Loading
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId == null) {
                _state.value = QuotaManagementState.Error("Utilisateur non connecte")
                return@launch
            }
            when (val result = quotaRepository.getQuotas(userId)) {
                is AppResult.Success -> {
                    _state.value = QuotaManagementState.Success(
                        quotas = result.data.map { it.toUiModel() },
                    )
                }
                is AppResult.Error -> {
                    _state.value = QuotaManagementState.Error(result.message)
                }
            }
        }
    }

    fun onStartEdit(nutrimentType: NutrimentType) {
        _editingNutriment.value = nutrimentType
        val currentState = _state.value
        if (currentState is QuotaManagementState.Success) {
            val quota = currentState.quotas.find { it.nutrimentType == nutrimentType }
            _editValue.value = quota?.valeurCible?.toString() ?: ""
        }
    }

    fun onEditValueChanged(value: String) {
        _editValue.value = value
    }

    fun onCancelEdit() {
        _editingNutriment.value = null
        _editValue.value = ""
    }

    fun onSaveEdit() {
        val nutrimentType = _editingNutriment.value ?: return
        val newValue = _editValue.value.toDoubleOrNull() ?: return

        viewModelScope.launch {
            val userId = getCurrentUserId() ?: return@launch
            val request = UpdateQuotaRequest(valeurCible = newValue)
            when (val result = quotaRepository.updateQuota(userId, nutrimentType, request)) {
                is AppResult.Success -> {
                    // Mettre a jour le quota modifie dans le state
                    val updatedQuota = result.data.toUiModel()
                    val currentState = _state.value
                    if (currentState is QuotaManagementState.Success) {
                        _state.update {
                            val updatedQuotas = currentState.quotas.map { quota ->
                                if (quota.nutrimentType == nutrimentType) updatedQuota else quota
                            }
                            QuotaManagementState.Success(quotas = updatedQuotas)
                        }
                    }
                }
                is AppResult.Error -> {
                    // En cas d'erreur, on garde l'etat actuel (l'utilisateur verra le dialog ferme)
                }
            }
            _editingNutriment.value = null
            _editValue.value = ""
        }
    }

    fun onResetQuota(nutrimentType: NutrimentType) {
        viewModelScope.launch {
            val userId = getCurrentUserId() ?: return@launch
            when (val result = quotaRepository.resetQuota(userId, nutrimentType)) {
                is AppResult.Success -> {
                    val updatedQuota = result.data.toUiModel()
                    val currentState = _state.value
                    if (currentState is QuotaManagementState.Success) {
                        _state.update {
                            val updatedQuotas = currentState.quotas.map { quota ->
                                if (quota.nutrimentType == nutrimentType) updatedQuota else quota
                            }
                            QuotaManagementState.Success(quotas = updatedQuotas)
                        }
                    }
                }
                is AppResult.Error -> {
                    // Silently ignore — quota reste inchange
                }
            }
        }
    }

    fun onResetAllQuotas() {
        viewModelScope.launch {
            val userId = getCurrentUserId() ?: return@launch
            when (val result = quotaRepository.resetAllQuotas(userId)) {
                is AppResult.Success -> {
                    _state.value = QuotaManagementState.Success(
                        quotas = result.data.map { it.toUiModel() },
                    )
                }
                is AppResult.Error -> {
                    // Silently ignore — quotas restent inchanges
                }
            }
        }
    }

    fun retry() {
        loadQuotas()
    }

    /**
     * Convertit un QuotaJournalier du domaine en QuotaUiModel pour l'affichage.
     */
    private fun QuotaJournalier.toUiModel(): QuotaUiModel {
        return QuotaUiModel(
            nutrimentType = nutriment,
            label = nutriment.toLabel(),
            valeurCible = valeurCible,
            valeurCalculee = valeurCalculee,
            unite = unite,
            estPersonnalise = estPersonnalise,
        )
    }

    private fun NutrimentType.toLabel(): String = when (this) {
        NutrimentType.CALORIES -> "Calories"
        NutrimentType.PROTEINES -> "Proteines"
        NutrimentType.GLUCIDES -> "Glucides"
        NutrimentType.LIPIDES -> "Lipides"
        NutrimentType.FIBRES -> "Fibres"
        NutrimentType.SEL -> "Sel"
        NutrimentType.SUCRES -> "Sucres"
        NutrimentType.FER -> "Fer"
        NutrimentType.CALCIUM -> "Calcium"
        NutrimentType.ZINC -> "Zinc"
        NutrimentType.MAGNESIUM -> "Magnesium"
        NutrimentType.VITAMINE_B12 -> "Vitamine B12"
        NutrimentType.VITAMINE_D -> "Vitamine D"
        NutrimentType.VITAMINE_C -> "Vitamine C"
        NutrimentType.OMEGA_3 -> "Omega-3"
        NutrimentType.OMEGA_6 -> "Omega-6"
    }
}

// --- States ---

sealed interface QuotaManagementState {
    data object Loading : QuotaManagementState
    data class Success(val quotas: List<QuotaUiModel>) : QuotaManagementState
    data class Error(val message: String) : QuotaManagementState
}

data class QuotaUiModel(
    val nutrimentType: NutrimentType,
    val label: String,
    val valeurCible: Double,
    val valeurCalculee: Double,
    val unite: String,
    val estPersonnalise: Boolean,
)
