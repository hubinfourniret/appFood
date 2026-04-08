package com.appfood.shared.ui.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.response.UserExportResponse
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.model.NiveauActivite
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.Sexe
import com.appfood.shared.ui.Strings
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for profile screens (PROFIL-02, PROFIL-03, PROFIL-04).
 * Manages profile editing, preferences, and account deletion.
 */
class ProfilViewModel(
    private val userRepository: UserRepository? = null,
    // TODO: Inject use cases when created by SHARED agent
    // private val getProfileUseCase: GetProfileUseCase,
    // private val updateProfileUseCase: UpdateProfileUseCase,
    // private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    // private val deleteAccountUseCase: DeleteAccountUseCase,
    // private val searchAlimentUseCase: SearchAlimentUseCase,
) : ViewModel() {

    companion object {
        const val MIN_AGE = 1
        const val MAX_AGE = 120
        const val MIN_POIDS = 20.0
        const val MAX_POIDS = 500.0
        const val MIN_TAILLE = 50
        const val MAX_TAILLE = 300
    }

    private val _state = MutableStateFlow<ProfilState>(ProfilState.Loading)
    val state: StateFlow<ProfilState> = _state.asStateFlow()

    // Edit profile fields
    private val _sexe = MutableStateFlow<Sexe?>(null)
    val sexe: StateFlow<Sexe?> = _sexe.asStateFlow()

    private val _ageText = MutableStateFlow("")
    val ageText: StateFlow<String> = _ageText.asStateFlow()

    private val _poidsText = MutableStateFlow("")
    val poidsText: StateFlow<String> = _poidsText.asStateFlow()

    private val _tailleText = MutableStateFlow("")
    val tailleText: StateFlow<String> = _tailleText.asStateFlow()

    private val _regimeAlimentaire = MutableStateFlow<RegimeAlimentaire?>(null)
    val regimeAlimentaire: StateFlow<RegimeAlimentaire?> = _regimeAlimentaire.asStateFlow()

    private val _niveauActivite = MutableStateFlow<NiveauActivite?>(null)
    val niveauActivite: StateFlow<NiveauActivite?> = _niveauActivite.asStateFlow()

    // Validation
    private val _editError = MutableStateFlow<String?>(null)
    val editError: StateFlow<String?> = _editError.asStateFlow()

    // Preferences
    private val _selectedAllergies = MutableStateFlow<Set<String>>(emptySet())
    val selectedAllergies: StateFlow<Set<String>> = _selectedAllergies.asStateFlow()

    private val _excludedAliments = MutableStateFlow<List<String>>(emptyList())
    val excludedAliments: StateFlow<List<String>> = _excludedAliments.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults.asStateFlow()

    // Save state
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    // Export state
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _state.value = ProfilState.Loading
        viewModelScope.launch {
            // TODO: Call getProfileUseCase when created by SHARED agent
            // val result = getProfileUseCase()
            // when (result) {
            //     is AppResult.Success -> {
            //         populateFields(result.data)
            //         _state.value = ProfilState.Loaded
            //     }
            //     is AppResult.Error -> _state.value = ProfilState.Error(result.message)
            // }

            // Stub: simulate loaded with default values
            _sexe.value = Sexe.HOMME
            _ageText.value = "30"
            _poidsText.value = "75.0"
            _tailleText.value = "178"
            _regimeAlimentaire.value = RegimeAlimentaire.VEGAN
            _niveauActivite.value = NiveauActivite.MODERE
            _state.value = ProfilState.Loaded
        }
    }

    // --- Edit profile setters ---

    fun onSexeChanged(sexe: Sexe) {
        _sexe.value = sexe
        _editError.value = null
    }

    fun onAgeChanged(value: String) {
        _ageText.value = value.filter { it.isDigit() }
        _editError.value = null
    }

    fun onPoidsChanged(value: String) {
        _poidsText.value = value.filter { it.isDigit() || it == '.' }
        _editError.value = null
    }

    fun onTailleChanged(value: String) {
        _tailleText.value = value.filter { it.isDigit() }
        _editError.value = null
    }

    fun onRegimeChanged(regime: RegimeAlimentaire) {
        _regimeAlimentaire.value = regime
    }

    fun onActiviteChanged(niveau: NiveauActivite) {
        _niveauActivite.value = niveau
    }

    fun onSaveProfile() {
        if (!validateEditForm()) return

        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            // TODO: Call updateProfileUseCase when created by SHARED agent
            // val result = updateProfileUseCase(
            //     UpdateProfileRequest(
            //         sexe = sexe.value?.name,
            //         age = ageText.value.toIntOrNull(),
            //         poidsKg = poidsText.value.toDoubleOrNull(),
            //         tailleCm = tailleText.value.toIntOrNull(),
            //         regimeAlimentaire = regimeAlimentaire.value?.name,
            //         niveauActivite = niveauActivite.value?.name,
            //     )
            // )

            // Stub: simulate success
            _saveState.value = SaveState.Success(Strings.PROFIL_SAVE_SUCCESS)
        }
    }

    private fun validateEditForm(): Boolean {
        val age = _ageText.value.toIntOrNull()
        val poids = _poidsText.value.toDoubleOrNull()
        val taille = _tailleText.value.toIntOrNull()

        if (age != null && (age < MIN_AGE || age > MAX_AGE)) {
            _editError.value = Strings.VALIDATION_AGE_RANGE
            return false
        }
        if (poids != null && (poids < MIN_POIDS || poids > MAX_POIDS)) {
            _editError.value = Strings.VALIDATION_POIDS_RANGE
            return false
        }
        if (taille != null && (taille < MIN_TAILLE || taille > MAX_TAILLE)) {
            _editError.value = Strings.VALIDATION_TAILLE_RANGE
            return false
        }
        return true
    }

    // --- Preferences ---

    fun onAllergieToggled(allergie: String) {
        _selectedAllergies.value = _selectedAllergies.value.toMutableSet().apply {
            if (contains(allergie)) remove(allergie) else add(allergie)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.length >= 2) {
            searchAliments(query)
        } else {
            _searchResults.value = emptyList()
        }
    }

    private fun searchAliments(query: String) {
        viewModelScope.launch {
            // TODO: Call searchAlimentUseCase when created by SHARED agent
            // val result = searchAlimentUseCase(query)
            // when (result) {
            //     is AppResult.Success -> _searchResults.value = result.data.map { it.nom }
            //     is AppResult.Error -> { /* silently ignore search errors */ }
            // }

            // Stub: empty results
            _searchResults.value = emptyList()
        }
    }

    fun onExcludedAlimentAdded(aliment: String) {
        if (aliment.isNotBlank() && aliment !in _excludedAliments.value) {
            _excludedAliments.value = _excludedAliments.value + aliment
        }
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun onExcludedAlimentRemoved(aliment: String) {
        _excludedAliments.value = _excludedAliments.value - aliment
    }

    fun onSavePreferences() {
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            // TODO: Call updatePreferencesUseCase when created by SHARED agent
            // val result = updatePreferencesUseCase(
            //     UpdatePreferencesRequest(
            //         allergies = selectedAllergies.value.toList(),
            //         alimentsExclus = excludedAliments.value,
            //     )
            // )

            // Stub: simulate success
            _saveState.value = SaveState.Success(Strings.PREFERENCES_SAVE_SUCCESS)
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    // --- Export data ---

    fun onExportData() {
        val repo = userRepository ?: run {
            _exportState.value = ExportState.Error(Strings.PROFIL_EXPORT_ERROR)
            return
        }
        _exportState.value = ExportState.Loading
        viewModelScope.launch {
            when (val result = repo.exportData()) {
                is AppResult.Success -> {
                    _exportState.value = ExportState.Success(result.data)
                }
                is AppResult.Error -> {
                    _exportState.value = ExportState.Error(result.message)
                }
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}

/**
 * Sealed interface for profile loading state.
 */
sealed interface ProfilState {
    data object Loading : ProfilState
    data object Loaded : ProfilState
    data class Error(val message: String) : ProfilState
}

/**
 * Sealed interface for save operation state.
 */
sealed interface SaveState {
    data object Idle : SaveState
    data object Saving : SaveState
    data class Success(val message: String) : SaveState
    data class Error(val message: String) : SaveState
}

/**
 * Sealed interface for data export state.
 */
sealed interface ExportState {
    data object Idle : ExportState
    data object Loading : ExportState
    data class Success(val data: UserExportResponse) : ExportState
    data class Error(val message: String) : ExportState
}
