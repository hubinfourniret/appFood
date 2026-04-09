package com.appfood.shared.ui.hydratation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.data.repository.HydratationRepository
import com.appfood.shared.domain.hydratation.AjouterEauUseCase
import com.appfood.shared.domain.hydratation.GetHydratationJourUseCase
import com.appfood.shared.domain.hydratation.UpdateObjectifHydratationUseCase
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * ViewModel for hydration tracking (HYDRA-01).
 * Manages water intake, objective, and weekly history.
 */
class HydratationViewModel(
    private val getHydratationJourUseCase: GetHydratationJourUseCase,
    private val ajouterEauUseCase: AjouterEauUseCase,
    private val updateObjectifUseCase: UpdateObjectifHydratationUseCase,
    private val hydratationRepository: HydratationRepository,
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
                val result = updateObjectifUseCase(USER_ID, newObjectif)
                when (result) {
                    is AppResult.Success -> {
                        // Reload today to get updated objectif from server
                        loadHydratation()
                    }
                    is AppResult.Error -> {
                        // Optimistic local update on error (offline)
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
                    }
                }
                _showObjectifDialog.value = false
            }
        }
    }

    fun resetObjectif() {
        viewModelScope.launch {
            val result = updateObjectifUseCase.reset(USER_ID)
            when (result) {
                is AppResult.Success -> {
                    loadHydratation()
                }
                is AppResult.Error -> {
                    // Optimistic local update on error (offline)
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
        }
    }

    fun retry() {
        loadHydratation()
    }

    private fun addWater(ml: Int) {
        viewModelScope.launch {
            val result = ajouterEauUseCase(USER_ID, ml)
            when (result) {
                is AppResult.Success -> {
                    val data = result.data
                    val current = _state.value
                    val objectifMl = (current as? HydratationState.Success)?.objectifMl ?: data.objectifMl
                    val pourcentage = if (objectifMl > 0) {
                        (data.quantiteMl.toDouble() / objectifMl * 100).coerceIn(0.0, 200.0)
                    } else 0.0
                    _state.value = HydratationState.Success(
                        quantiteMl = data.quantiteMl,
                        objectifMl = objectifMl,
                        pourcentage = pourcentage,
                        estPersonnalise = data.estObjectifPersonnalise,
                        entrees = data.entrees.map { entry ->
                            HydratationEntree(
                                heure = formatHeure(entry.heure),
                                quantiteMl = entry.quantiteMl,
                            )
                        },
                        weeklyData = (current as? HydratationState.Success)?.weeklyData
                            ?: listOf(0, 0, 0, 0, 0, 0, 0),
                    )
                }
                is AppResult.Error -> {
                    // Optimistic local update on error (offline)
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
        }
    }

    private fun loadHydratation() {
        _state.value = HydratationState.Loading
        viewModelScope.launch {
            val result = getHydratationJourUseCase(USER_ID)
            when (result) {
                is AppResult.Success -> {
                    val data = result.data
                    val pourcentage = if (data.objectifMl > 0) {
                        (data.quantiteMl.toDouble() / data.objectifMl * 100).coerceIn(0.0, 200.0)
                    } else 0.0

                    // Load weekly data in parallel
                    val weeklyData = loadWeeklyData()

                    _state.value = HydratationState.Success(
                        quantiteMl = data.quantiteMl,
                        objectifMl = data.objectifMl,
                        pourcentage = pourcentage,
                        estPersonnalise = data.estObjectifPersonnalise,
                        entrees = data.entrees.map { entry ->
                            HydratationEntree(
                                heure = formatHeure(entry.heure),
                                quantiteMl = entry.quantiteMl,
                            )
                        },
                        weeklyData = weeklyData,
                    )
                }
                is AppResult.Error -> {
                    _state.value = HydratationState.Error(result.message)
                }
            }
        }
    }

    /**
     * Charge les totaux d'hydratation des 7 derniers jours.
     */
    private suspend fun loadWeeklyData(): List<Int> {
        val tz = TimeZone.currentSystemDefault()
        val today = kotlinx.datetime.Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()).toLocalDateTime(tz).date
        val weekAgo = today.minus(6, DateTimeUnit.DAY)
        val result = hydratationRepository.getWeekly(USER_ID, weekAgo.toString(), today.toString())
        return when (result) {
            is AppResult.Success -> {
                // Build a list of 7 days, filling missing days with 0
                val dataByDate = result.data.associateBy { it.date }
                (0..6).map { offset ->
                    val date = weekAgo.plus(offset, DateTimeUnit.DAY)
                    dataByDate[date]?.quantiteMl ?: 0
                }
            }
            is AppResult.Error -> listOf(0, 0, 0, 0, 0, 0, 0)
        }
    }

    private fun formatHeure(instant: kotlin.time.Instant): String {
        val kxInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(instant.toEpochMilliseconds())
        val localDateTime = kxInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    companion object {
        const val GLASS_ML = 250
        const val BOTTLE_ML = 500
        const val DEFAULT_OBJECTIF_ML = 2000
        // userId is resolved by the backend from the JWT token;
        // this placeholder is passed through the use case / repository layer.
        private const val USER_ID = "me"
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
