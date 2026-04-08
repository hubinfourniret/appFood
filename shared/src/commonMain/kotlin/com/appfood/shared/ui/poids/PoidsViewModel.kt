package com.appfood.shared.ui.poids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.domain.poids.DetecterChangementPoidsUseCase
import com.appfood.shared.domain.poids.EnregistrerPoidsUseCase
import com.appfood.shared.domain.poids.GetHistoriquePoidsUseCase
import com.appfood.shared.domain.poids.RecalculerQuotasApresPoidsUseCase
import com.appfood.shared.ui.Strings
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for weight tracking screen (POIDS-01 + POIDS-02).
 * Manages weight entry, history display, period filtering,
 * significant change detection, and quota recalculation.
 */
class PoidsViewModel(
    private val enregistrerPoidsUseCase: EnregistrerPoidsUseCase,
    private val getHistoriquePoidsUseCase: GetHistoriquePoidsUseCase,
    private val detecterChangementPoidsUseCase: DetecterChangementPoidsUseCase,
    private val recalculerQuotasApresPoidsUseCase: RecalculerQuotasApresPoidsUseCase,
    private val userRepository: UserRepository,
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

    /** Cached userId fetched once from UserRepository. */
    private var cachedUserId: String? = null

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
            val userId = getUserId()
            if (userId == null) {
                _saveMessage.value = Strings.POIDS_SAVE_ERROR
                _isSaving.value = false
                return@launch
            }

            when (val result = enregistrerPoidsUseCase(userId = userId, poidsKg = poidsValue)) {
                is AppResult.Success -> {
                    _saveMessage.value = Strings.POIDS_SAVE_SUCCESS
                    _inputPoids.value = ""
                    _isSaving.value = false
                    loadHistorique()
                    checkRecalculQuotas(userId)
                }
                is AppResult.Error -> {
                    _saveMessage.value = result.message
                    _isSaving.value = false
                }
            }
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
     * Utilise DetecterChangementPoidsUseCase pour detecter un changement significatif (>1 kg).
     */
    private fun checkRecalculQuotas(userId: String) {
        viewModelScope.launch {
            when (val result = detecterChangementPoidsUseCase(userId)) {
                is AppResult.Success -> {
                    if (result.data.changementSignificatif) {
                        _showRecalculDialog.value = true
                    }
                }
                is AppResult.Error -> {
                    // Silent fail — detection is non-blocking
                }
            }
        }
    }

    fun onRecalculAccepted() {
        _showRecalculDialog.value = false
        _isRecalculating.value = true
        viewModelScope.launch {
            val userId = getUserId()
            if (userId == null) {
                _isRecalculating.value = false
                return@launch
            }

            when (val result = recalculerQuotasApresPoidsUseCase(userId)) {
                is AppResult.Success -> {
                    _saveMessage.value = if (result.data.recalculated) {
                        result.data.message
                    } else {
                        Strings.POIDS_RECALCUL_SUCCESS
                    }
                }
                is AppResult.Error -> {
                    _saveMessage.value = result.message
                }
            }
            _isRecalculating.value = false
        }
    }

    fun onRecalculDismissed() {
        _showRecalculDialog.value = false
    }

    private fun loadHistorique() {
        _state.value = PoidsState.Loading
        viewModelScope.launch {
            val userId = getUserId()
            if (userId == null) {
                _state.value = PoidsState.Error("Utilisateur non connecte")
                return@launch
            }

            // Compute date range based on selected period
            val nowMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val kxInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(nowMs)
            val today = kxInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val dateFrom = computeDateFrom(today, _selectedPeriod.value)

            when (val result = getHistoriquePoidsUseCase(
                userId = userId,
                dateFrom = dateFrom.toString(),
                dateTo = today.toString(),
            )) {
                is AppResult.Success -> {
                    val entries = result.data.map { hp ->
                        PoidsEntry(
                            date = hp.date.toString(),
                            poids = hp.poidsKg,
                            timestampMillis = hp.createdAt.toEpochMilliseconds(),
                        )
                    }
                    _state.value = PoidsState.Success(
                        entries = entries,
                        poidsCourant = entries.lastOrNull()?.poids,
                        poidsMin = entries.minByOrNull { it.poids }?.poids,
                        poidsMax = entries.maxByOrNull { it.poids }?.poids,
                    )
                }
                is AppResult.Error -> {
                    _state.value = PoidsState.Error(result.message)
                }
            }
        }
    }

    /**
     * Fetches and caches the current user ID from UserRepository.
     */
    private suspend fun getUserId(): String? {
        cachedUserId?.let { return it }
        return when (val result = userRepository.getCurrentUser()) {
            is AppResult.Success -> {
                val id = result.data.user.id
                cachedUserId = id
                id
            }
            is AppResult.Error -> null
        }
    }

    companion object {
        private const val MIN_POIDS = 20.0
        private const val MAX_POIDS = 500.0

        /**
         * Computes the start date for the history range based on the selected period.
         */
        fun computeDateFrom(today: LocalDate, period: PoidsPeriod): LocalDate {
            return when (period) {
                PoidsPeriod.WEEK -> today.minus(DatePeriod(days = 7))
                PoidsPeriod.MONTH -> today.minus(DatePeriod(months = 1))
                PoidsPeriod.THREE_MONTHS -> today.minus(DatePeriod(months = 3))
                PoidsPeriod.SIX_MONTHS -> today.minus(DatePeriod(months = 6))
                PoidsPeriod.YEAR -> today.minus(DatePeriod(years = 1))
            }
        }
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
