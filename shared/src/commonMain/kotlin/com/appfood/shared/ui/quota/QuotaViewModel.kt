package com.appfood.shared.ui.quota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.model.NutrimentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the quota management screen (QUOTAS-02).
 * Allows users to view and customize their daily nutriment quotas.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class QuotaViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val getQuotasUseCase: GetQuotasUseCase,
    // private val updateQuotaUseCase: UpdateQuotaUseCase,
    // private val resetQuotaUseCase: ResetQuotaUseCase,
    // private val resetAllQuotasUseCase: ResetAllQuotasUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<QuotaManagementState>(QuotaManagementState.Loading)
    val state: StateFlow<QuotaManagementState> = _state.asStateFlow()

    private val _editingNutriment = MutableStateFlow<NutrimentType?>(null)
    val editingNutriment: StateFlow<NutrimentType?> = _editingNutriment.asStateFlow()

    private val _editValue = MutableStateFlow("")
    val editValue: StateFlow<String> = _editValue.asStateFlow()

    fun loadQuotas() {
        _state.value = QuotaManagementState.Loading
        viewModelScope.launch {
            // TODO: Call getQuotasUseCase when created by SHARED agent
            // val result = getQuotasUseCase()
            // when (result) {
            //     is AppResult.Success -> {
            //         _state.value = QuotaManagementState.Success(result.data)
            //     }
            //     is AppResult.Error -> {
            //         _state.value = QuotaManagementState.Error(result.message)
            //     }
            // }

            // Stub: simulate success with default quotas
            _state.value = QuotaManagementState.Success(
                quotas = buildStubQuotas(),
            )
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
            // TODO: Call updateQuotaUseCase when created by SHARED agent
            // val result = updateQuotaUseCase(nutrimentType, newValue)

            // Stub: update locally
            val currentState = _state.value
            if (currentState is QuotaManagementState.Success) {
                _state.update {
                    val updatedQuotas = currentState.quotas.map { quota ->
                        if (quota.nutrimentType == nutrimentType) {
                            quota.copy(
                                valeurCible = newValue,
                                estPersonnalise = true,
                            )
                        } else {
                            quota
                        }
                    }
                    QuotaManagementState.Success(quotas = updatedQuotas)
                }
            }

            _editingNutriment.value = null
            _editValue.value = ""
        }
    }

    fun onResetQuota(nutrimentType: NutrimentType) {
        viewModelScope.launch {
            // TODO: Call resetQuotaUseCase when created by SHARED agent
            // val result = resetQuotaUseCase(nutrimentType)

            // Stub: reset locally
            val currentState = _state.value
            if (currentState is QuotaManagementState.Success) {
                _state.update {
                    val updatedQuotas = currentState.quotas.map { quota ->
                        if (quota.nutrimentType == nutrimentType) {
                            quota.copy(
                                valeurCible = quota.valeurCalculee,
                                estPersonnalise = false,
                            )
                        } else {
                            quota
                        }
                    }
                    QuotaManagementState.Success(quotas = updatedQuotas)
                }
            }
        }
    }

    fun onResetAllQuotas() {
        viewModelScope.launch {
            // TODO: Call resetAllQuotasUseCase when created by SHARED agent
            // val result = resetAllQuotasUseCase()

            // Stub: reset all locally
            val currentState = _state.value
            if (currentState is QuotaManagementState.Success) {
                _state.update {
                    val updatedQuotas = currentState.quotas.map { quota ->
                        quota.copy(
                            valeurCible = quota.valeurCalculee,
                            estPersonnalise = false,
                        )
                    }
                    QuotaManagementState.Success(quotas = updatedQuotas)
                }
            }
        }
    }

    fun retry() {
        loadQuotas()
    }

    private fun buildStubQuotas(): List<QuotaUiModel> = listOf(
        QuotaUiModel(NutrimentType.CALORIES, "Calories", 2759.0, 2759.0, "kcal", false),
        QuotaUiModel(NutrimentType.PROTEINES, "Proteines", 82.0, 82.0, "g", false),
        QuotaUiModel(NutrimentType.GLUCIDES, "Glucides", 345.0, 345.0, "g", false),
        QuotaUiModel(NutrimentType.LIPIDES, "Lipides", 92.0, 92.0, "g", false),
        QuotaUiModel(NutrimentType.FIBRES, "Fibres", 30.0, 30.0, "g", false),
        QuotaUiModel(NutrimentType.VITAMINE_B12, "Vitamine B12", 2.4, 2.4, "ug", false),
        QuotaUiModel(NutrimentType.VITAMINE_D, "Vitamine D", 15.0, 15.0, "ug", false),
        QuotaUiModel(NutrimentType.VITAMINE_C, "Vitamine C", 110.0, 110.0, "mg", false),
        QuotaUiModel(NutrimentType.FER, "Fer", 11.0, 11.0, "mg", false),
        QuotaUiModel(NutrimentType.CALCIUM, "Calcium", 900.0, 900.0, "mg", false),
        QuotaUiModel(NutrimentType.ZINC, "Zinc", 12.0, 12.0, "mg", false),
        QuotaUiModel(NutrimentType.MAGNESIUM, "Magnesium", 420.0, 420.0, "mg", false),
        QuotaUiModel(NutrimentType.OMEGA_3, "Omega-3", 2.5, 2.5, "g", false),
        QuotaUiModel(NutrimentType.OMEGA_6, "Omega-6", 10.0, 10.0, "g", false),
    )
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
