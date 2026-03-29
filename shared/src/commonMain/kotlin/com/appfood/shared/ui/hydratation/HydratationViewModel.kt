package com.appfood.shared.ui.hydratation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for hydration tracking (HYDRA-01).
 * Manages water intake, objective, and weekly history.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class HydratationViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val getHydratationJourUseCase: GetHydratationJourUseCase,
    // private val ajouterEauUseCase: AjouterEauUseCase,
    // private val updateObjectifUseCase: UpdateObjectifHydratationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<HydratationState>(HydratationState.Loading)
    val state: StateFlow<HydratationState> = _state.asStateFlow()

    // --- Custom input ---
    private val _customInput = MutableStateFlow("")
    val customInput: StateFlow<String> = _customInput.asStateFlow()

    private val _objectifInput = MutableStateFlow("")
    val objectifInput: StateFlow<String> = _objectifInput.asStateFlow()

    private val _showCustomDialog = MutableStateFlow(false)
    val showCustomDialog: StateFlow<Boolean> = _showCustomDialog.asStateFlow()

    private val _showObjectifDialog = MutableStateFlow(false)
    val showObjectifDialog: StateFlow<Boolean> = _showObjectifDialog.asStateFlow()

    fun init() {
        loadHydratation()
    }

    fun addGlass() {
        addWater(GLASS_ML)
    }

    fun addBottle() {
        addWater(BOTTLE_ML)
    }

    fun showCustomInput() {
        _customInput.value = ""
        _showCustomDialog.value = true
    }

    fun dismissCustomDialog() {
        _showCustomDialog.value = false
    }

    fun onCustomInputChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,5}$"))) {
            _customInput.value = value
        }
    }

    fun addCustom() {
        val ml = _customInput.value.toIntOrNull() ?: return
        if (ml in 1..5000) {
            addWater(ml)
            _showCustomDialog.value = false
        }
    }

    fun showObjectifEditor() {
        val current = (_state.value as? HydratationState.Success)?.objectifMl ?: DEFAULT_OBJECTIF_ML
        _objectifInput.value = current.toString()
        _showObjectifDialog.value = true
    }

    fun dismissObjectifDialog() {
        _showObjectifDialog.value = false
    }

    fun onObjectifInputChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,5}$"))) {
            _objectifInput.value = value
        }
    }

    fun updateObjectif() {
        val newObjectif = _objectifInput.value.toIntOrNull() ?: return
        if (newObjectif in 500..10000) {
            viewModelScope.launch {
                // TODO: Call use case when created by SHARED agent
                // updateObjectifUseCase(newObjectif)

                // Stub: update state directly
                val current = _state.value
                if (current is HydratationState.Success) {
                    val pourcentage = if (newObjectif > 0) {
                        (current.quantiteMl.toDouble() / newObjectif * 100).coerceIn(0.0, 200.0)
                    } else 0.0
                    _state.value = current.copy(
                        objectifMl = newObjectif,
                        pourcentage = pourcentage,
                        estPersonnalise = true,
                    )
                }
                _showObjectifDialog.value = false
            }
        }
    }

    fun resetObjectif() {
        viewModelScope.launch {
            // TODO: Call use case when created by SHARED agent
            // resetObjectifUseCase()

            // Stub: reset to default
            val current = _state.value
            if (current is HydratationState.Success) {
                val pourcentage = if (DEFAULT_OBJECTIF_ML > 0) {
                    (current.quantiteMl.toDouble() / DEFAULT_OBJECTIF_ML * 100).coerceIn(0.0, 200.0)
                } else 0.0
                _state.value = current.copy(
                    objectifMl = DEFAULT_OBJECTIF_ML,
                    pourcentage = pourcentage,
                    estPersonnalise = false,
                )
            }
        }
    }

    fun retry() {
        loadHydratation()
    }

    private fun addWater(ml: Int) {
        viewModelScope.launch {
            // TODO: Call use case when created by SHARED agent
            // val result = ajouterEauUseCase(ml)

            // Stub: update state directly
            val current = _state.value
            if (current is HydratationState.Success) {
                val newQuantite = current.quantiteMl + ml
                val pourcentage = if (current.objectifMl > 0) {
                    (newQuantite.toDouble() / current.objectifMl * 100).coerceIn(0.0, 200.0)
                } else 0.0
                val newEntree = HydratationEntree(
                    heure = "Maintenant",
                    quantiteMl = ml,
                )
                _state.value = current.copy(
                    quantiteMl = newQuantite,
                    pourcentage = pourcentage,
                    entrees = current.entrees + newEntree,
                )
            }
        }
    }

    private fun loadHydratation() {
        _state.value = HydratationState.Loading
        viewModelScope.launch {
            // TODO: Call use cases when created by SHARED agent
            // val result = getHydratationJourUseCase()

            // Stub: simulate success with empty data
            _state.value = HydratationState.Success(
                quantiteMl = 0,
                objectifMl = DEFAULT_OBJECTIF_ML,
                pourcentage = 0.0,
                estPersonnalise = false,
                entrees = emptyList(),
                weeklyData = listOf(0, 0, 0, 0, 0, 0, 0),
            )
        }
    }

    companion object {
        const val GLASS_ML = 250
        const val BOTTLE_ML = 500
        const val DEFAULT_OBJECTIF_ML = 2000
    }
}

// --- States ---

sealed interface HydratationState {
    data object Loading : HydratationState
    data class Success(
        val quantiteMl: Int,
        val objectifMl: Int,
        val pourcentage: Double,
        val estPersonnalise: Boolean,
        val entrees: List<HydratationEntree>,
        val weeklyData: List<Int>,
    ) : HydratationState
    data class Error(val message: String) : HydratationState
}

data class HydratationEntree(
    val heure: String,
    val quantiteMl: Int,
)
