package com.appfood.shared.ui.poids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.ui.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for weight tracking screen (POIDS-01).
 * Manages weight entry, history, and period selection.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class PoidsViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val enregistrerPoidsUseCase: EnregistrerPoidsUseCase,
    // private val getHistoriquePoidsUseCase: GetHistoriquePoidsUseCase,
    // private val recalculerQuotasApresPoidsUseCase: RecalculerQuotasApresPoidsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<PoidsState>(PoidsState.Loading)
    val state: StateFlow<PoidsState> = _state.asStateFlow()

    // --- Input state ---
    private val _inputPoids = MutableStateFlow("")
    val inputPoids: StateFlow<String> = _inputPoids.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    // --- Period selection ---
    private val _selectedPeriod = MutableStateFlow(PoidsPeriod.MONTH)
    val selectedPeriod: StateFlow<PoidsPeriod> = _selectedPeriod.asStateFlow()

    // --- POIDS-02 : Recalcul quotas apres changement de poids significatif ---
    private val _showRecalculDialog = MutableStateFlow(false)
    val showRecalculDialog: StateFlow<Boolean> = _showRecalculDialog.asStateFlow()

    private val _isRecalculating = MutableStateFlow(false)
    val isRecalculating: StateFlow<Boolean> = _isRecalculating.asStateFlow()

    fun init() {
        loadHistorique()
    }

    fun onPoidsInputChanged(value: String) {
        // Allow only valid numeric input
        if (value.isEmpty() || value.matches(Regex("^\\d{0,3}(\\.\\d{0,1})?$"))) {
            _inputPoids.value = value
        }
    }

    fun onPeriodChanged(period: PoidsPeriod) {
        _selectedPeriod.value = period
        loadHistorique()
    }

    fun onSavePoids() {
        val poidsValue = _inputPoids.value.toDoubleOrNull() ?: return
        if (poidsValue < MIN_POIDS || poidsValue > MAX_POIDS) return

        _isSaving.value = true
        viewModelScope.launch {
            // TODO: Call use case when created by SHARED agent
            // val result = enregistrerPoidsUseCase(poids = poidsValue)
            // when (result) {
            //     is AppResult.Success -> {
            //         _saveMessage.value = Strings.POIDS_SAVE_SUCCESS
            //         _inputPoids.value = ""
            //         loadHistorique()
            //         checkRecalculQuotas()
            //     }
            //     is AppResult.Error -> {
            //         _saveMessage.value = result.message
            //     }
            // }

            // Stub: simulate success
            _saveMessage.value = "Poids enregistre"
            _inputPoids.value = ""
            _isSaving.value = false
            loadHistorique()
            checkRecalculQuotas()
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    fun retry() {
        loadHistorique()
    }

    // --- POIDS-02 : Recalcul quotas ---

    /**
     * Verifie si un recalcul des quotas est necessaire apres un enregistrement de poids.
     */
    private fun checkRecalculQuotas() {
        viewModelScope.launch {
            // TODO: Call recalculerQuotasApresPoidsUseCase when created by SHARED agent
            // val result = recalculerQuotasApresPoidsUseCase(userId)
            // when (result) {
            //     is AppResult.Success -> {
            //         if (result.data.recalculated) {
            //             _saveMessage.value = Strings.POIDS_RECALCUL_SUCCESS
            //         } else {
            //             // Pas de changement significatif, proposer le recalcul via dialog
            //         }
            //     }
            //     is AppResult.Error -> { /* silent fail */ }
            // }

            // Stub: simulate significant weight change detected — show dialog
            _showRecalculDialog.value = true
        }
    }

    fun onRecalculAccepted() {
        _showRecalculDialog.value = false
        _isRecalculating.value = true
        viewModelScope.launch {
            // TODO: Call recalculerQuotasApresPoidsUseCase.invoke(userId) when created by SHARED agent
            // val result = recalculerQuotasApresPoidsUseCase(userId)
            // when (result) {
            //     is AppResult.Success -> {
            //         _saveMessage.value = Strings.POIDS_RECALCUL_SUCCESS
            //     }
            //     is AppResult.Error -> {
            //         _saveMessage.value = result.message
            //     }
            // }

            // Stub: simulate success
            _saveMessage.value = Strings.POIDS_RECALCUL_SUCCESS
            _isRecalculating.value = false
        }
    }

    fun onRecalculDismissed() {
        _showRecalculDialog.value = false
    }

    private fun loadHistorique() {
        _state.value = PoidsState.Loading
        viewModelScope.launch {
            // TODO: Call use case when created by SHARED agent
            // val result = getHistoriquePoidsUseCase(period = _selectedPeriod.value)
            // when (result) {
            //     is AppResult.Success -> {
            //         val entries = result.data
            //         _state.value = PoidsState.Success(
            //             entries = entries,
            //             poidsCourant = entries.lastOrNull()?.poids,
            //             poidsMin = entries.minByOrNull { it.poids }?.poids,
            //             poidsMax = entries.maxByOrNull { it.poids }?.poids,
            //         )
            //     }
            //     is AppResult.Error -> {
            //         _state.value = PoidsState.Error(result.message)
            //     }
            // }

            // Stub: empty data
            _state.value = PoidsState.Success(
                entries = emptyList(),
                poidsCourant = null,
                poidsMin = null,
                poidsMax = null,
            )
        }
    }

    companion object {
        private const val MIN_POIDS = 20.0
        private const val MAX_POIDS = 500.0
    }
}

// --- States ---

sealed interface PoidsState {
    data object Loading : PoidsState
    data class Success(
        val entries: List<PoidsEntry>,
        val poidsCourant: Double?,
        val poidsMin: Double?,
        val poidsMax: Double?,
    ) : PoidsState
    data class Error(val message: String) : PoidsState
}

data class PoidsEntry(
    val date: String,
    val poids: Double,
    val timestampMillis: Long,
)

enum class PoidsPeriod {
    WEEK, MONTH, THREE_MONTHS, SIX_MONTHS, YEAR
}
