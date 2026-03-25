package com.appfood.shared.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.model.NiveauActivite
import com.appfood.shared.model.RegimeAlimentaire
import com.appfood.shared.model.Sexe
import com.appfood.shared.ui.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the onboarding questionnaire (PROFIL-01).
 * Manages multi-step form state and calls the profile creation use case.
 */
class OnboardingViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val createProfileUseCase: CreateProfileUseCase,
    // private val updatePreferencesUseCase: UpdatePreferencesUseCase,
) : ViewModel() {

    companion object {
        const val TOTAL_STEPS = 4
        const val MIN_AGE = 1
        const val MAX_AGE = 120
        const val MIN_POIDS = 20.0
        const val MAX_POIDS = 500.0
        const val MIN_TAILLE = 50
        const val MAX_TAILLE = 300

        // Default values when user skips
        private val DEFAULT_SEXE = Sexe.HOMME
        private const val DEFAULT_AGE = 30
        private const val DEFAULT_POIDS = 70.0
        private const val DEFAULT_TAILLE = 170
        private val DEFAULT_REGIME = RegimeAlimentaire.OMNIVORE
        private val DEFAULT_ACTIVITE = NiveauActivite.MODERE

    }

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.InProgress)
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Step 1: Body metrics
    private val _sexe = MutableStateFlow<Sexe?>(null)
    val sexe: StateFlow<Sexe?> = _sexe.asStateFlow()

    private val _ageText = MutableStateFlow("")
    val ageText: StateFlow<String> = _ageText.asStateFlow()

    private val _poidsText = MutableStateFlow("")
    val poidsText: StateFlow<String> = _poidsText.asStateFlow()

    private val _tailleText = MutableStateFlow("")
    val tailleText: StateFlow<String> = _tailleText.asStateFlow()

    // Step 2: Diet type
    private val _regimeAlimentaire = MutableStateFlow<RegimeAlimentaire?>(null)
    val regimeAlimentaire: StateFlow<RegimeAlimentaire?> = _regimeAlimentaire.asStateFlow()

    // Step 3: Activity level
    private val _niveauActivite = MutableStateFlow<NiveauActivite?>(null)
    val niveauActivite: StateFlow<NiveauActivite?> = _niveauActivite.asStateFlow()

    // Step 4: Allergies and exclusions (optional)
    private val _selectedAllergies = MutableStateFlow<Set<String>>(emptySet())
    val selectedAllergies: StateFlow<Set<String>> = _selectedAllergies.asStateFlow()

    private val _excludedAliments = MutableStateFlow<List<String>>(emptyList())
    val excludedAliments: StateFlow<List<String>> = _excludedAliments.asStateFlow()

    // Validation errors
    private val _step1Error = MutableStateFlow<String?>(null)
    val step1Error: StateFlow<String?> = _step1Error.asStateFlow()

    // --- Step 1 setters ---

    fun onSexeChanged(sexe: Sexe) {
        _sexe.value = sexe
        _step1Error.value = null
    }

    fun onAgeChanged(value: String) {
        // Allow only digits
        _ageText.value = value.filter { it.isDigit() }
        _step1Error.value = null
    }

    fun onPoidsChanged(value: String) {
        // Allow digits and one decimal point
        _poidsText.value = value.filter { it.isDigit() || it == '.' }
        _step1Error.value = null
    }

    fun onTailleChanged(value: String) {
        _tailleText.value = value.filter { it.isDigit() }
        _step1Error.value = null
    }

    // --- Step 2 setter ---

    fun onRegimeChanged(regime: RegimeAlimentaire) {
        _regimeAlimentaire.value = regime
    }

    // --- Step 3 setter ---

    fun onActiviteChanged(niveau: NiveauActivite) {
        _niveauActivite.value = niveau
    }

    // --- Step 4 setters ---

    fun onAllergieToggled(allergie: String) {
        _selectedAllergies.value = _selectedAllergies.value.toMutableSet().apply {
            if (contains(allergie)) remove(allergie) else add(allergie)
        }
    }

    fun onExcludedAlimentAdded(aliment: String) {
        if (aliment.isNotBlank() && aliment !in _excludedAliments.value) {
            _excludedAliments.value = _excludedAliments.value + aliment
        }
    }

    fun onExcludedAlimentRemoved(aliment: String) {
        _excludedAliments.value = _excludedAliments.value - aliment
    }

    // --- Navigation ---

    fun onContinue() {
        when (_currentStep.value) {
            1 -> {
                if (validateStep1()) {
                    _currentStep.value = 2
                }
            }
            2 -> {
                // No mandatory selection — default is applied at save time
                _currentStep.value = 3
            }
            3 -> {
                _currentStep.value = 4
            }
            4 -> {
                saveProfile()
            }
        }
    }

    fun onBack() {
        if (_currentStep.value > 1) {
            _currentStep.value = _currentStep.value - 1
        }
    }

    fun onSkip() {
        when (_currentStep.value) {
            1 -> {
                // Apply defaults and move on
                _sexe.value = DEFAULT_SEXE
                _ageText.value = DEFAULT_AGE.toString()
                _poidsText.value = DEFAULT_POIDS.toString()
                _tailleText.value = DEFAULT_TAILLE.toString()
                _currentStep.value = 2
            }
            2 -> {
                _regimeAlimentaire.value = DEFAULT_REGIME
                _currentStep.value = 3
            }
            3 -> {
                _niveauActivite.value = DEFAULT_ACTIVITE
                _currentStep.value = 4
            }
            4 -> {
                // Skip allergies step entirely and save
                saveProfile()
            }
        }
    }

    private fun validateStep1(): Boolean {
        val age = _ageText.value.toIntOrNull()
        val poids = _poidsText.value.toDoubleOrNull()
        val taille = _tailleText.value.toIntOrNull()

        if (_sexe.value == null) {
            _step1Error.value = Strings.VALIDATION_FIELD_REQUIRED
            return false
        }
        if (age == null || age < MIN_AGE || age > MAX_AGE) {
            _step1Error.value = Strings.VALIDATION_AGE_RANGE
            return false
        }
        if (poids == null || poids < MIN_POIDS || poids > MAX_POIDS) {
            _step1Error.value = Strings.VALIDATION_POIDS_RANGE
            return false
        }
        if (taille == null || taille < MIN_TAILLE || taille > MAX_TAILLE) {
            _step1Error.value = Strings.VALIDATION_TAILLE_RANGE
            return false
        }
        return true
    }

    private fun saveProfile() {
        _state.value = OnboardingState.Saving
        viewModelScope.launch {
            // TODO: Call createProfileUseCase when created by SHARED agent
            // val result = createProfileUseCase(
            //     CreateProfileRequest(
            //         sexe = (sexe.value ?: DEFAULT_SEXE).name,
            //         age = ageText.value.toIntOrNull() ?: DEFAULT_AGE,
            //         poidsKg = poidsText.value.toDoubleOrNull() ?: DEFAULT_POIDS,
            //         tailleCm = tailleText.value.toIntOrNull() ?: DEFAULT_TAILLE,
            //         regimeAlimentaire = (regimeAlimentaire.value ?: DEFAULT_REGIME).name,
            //         niveauActivite = (niveauActivite.value ?: DEFAULT_ACTIVITE).name,
            //     )
            // )
            //
            // If allergies/exclusions selected, also call updatePreferencesUseCase
            // if (selectedAllergies.value.isNotEmpty() || excludedAliments.value.isNotEmpty()) {
            //     updatePreferencesUseCase(
            //         UpdatePreferencesRequest(
            //             allergies = selectedAllergies.value.toList(),
            //             alimentsExclus = excludedAliments.value,
            //         )
            //     )
            // }

            // Stub: simulate success
            _state.value = OnboardingState.Complete
        }
    }
}

/**
 * Sealed interface representing onboarding states.
 */
sealed interface OnboardingState {
    data object InProgress : OnboardingState
    data object Saving : OnboardingState
    data object Complete : OnboardingState
    data class Error(val message: String) : OnboardingState
}
