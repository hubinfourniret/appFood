package com.appfood.shared.ui.social

import com.appfood.shared.api.request.UpdateSocialProfileRequest
import com.appfood.shared.api.response.UserProfileResponse
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.model.SocialVisibility
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * TACHE-600 : ViewModel pour SocialOnboardingScreen et SocialSettingsScreen.
 *
 * Trois modes :
 * - Onboarding (handle/dateNaissance/visibility a saisir, dateNaissance editable)
 * - Settings (handle/bio/visibility editables, dateNaissance verrouillee)
 */
class SocialProfileViewModel(
    private val userRepository: UserRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _form = MutableStateFlow(SocialFormState())
    val form: StateFlow<SocialFormState> = _form.asStateFlow()

    private val _state = MutableStateFlow<SocialSubmitState>(SocialSubmitState.Idle)
    val state: StateFlow<SocialSubmitState> = _state.asStateFlow()

    private val _handleCheck = MutableStateFlow(HandleCheckState.Idle)
    val handleCheck: StateFlow<HandleCheckState> = _handleCheck.asStateFlow()

    private var handleCheckJob: Job? = null
    private var ownedHandle: String? = null // handle existant du user (mode settings)

    /**
     * Pre-remplit le formulaire avec les donnees existantes (mode settings).
     * @param dateLocked si true, la date de naissance ne peut pas etre modifiee
     */
    fun prefillFromUser(
        handle: String?,
        bio: String?,
        dateNaissanceIso: String?,
        visibility: SocialVisibility,
        dateLocked: Boolean,
    ) {
        val (day, month, year) = parseDate(dateNaissanceIso)
        ownedHandle = handle?.lowercase()
        _form.value = SocialFormState(
            handle = handle ?: "",
            bio = bio ?: "",
            day = day,
            month = month,
            year = year,
            visibility = visibility,
            dateLocked = dateLocked,
        )
        if (handle != null) {
            _handleCheck.value = HandleCheckState.Owned
        }
    }

    fun onHandleChanged(value: String) {
        val trimmed = value.trim()
        _form.update { it.copy(handle = trimmed) }
        handleCheckJob?.cancel()
        // Cas : c'est le handle deja possede par le user
        if (ownedHandle != null && trimmed.lowercase() == ownedHandle) {
            _handleCheck.value = HandleCheckState.Owned
            return
        }
        if (trimmed.isEmpty()) {
            _handleCheck.value = HandleCheckState.Idle
            return
        }
        if (!HANDLE_REGEX.matches(trimmed)) {
            _handleCheck.value = HandleCheckState.Invalid
            return
        }
        _handleCheck.value = HandleCheckState.Checking
        handleCheckJob = scope.launch {
            delay(400)
            when (val r = userRepository.checkHandleAvailable(trimmed)) {
                is AppResult.Success ->
                    _handleCheck.value = if (r.data) HandleCheckState.Available else HandleCheckState.Taken
                is AppResult.Error -> _handleCheck.value = HandleCheckState.Idle
            }
        }
    }

    fun onBioChanged(value: String) {
        _form.update { it.copy(bio = value.take(280)) }
    }

    fun onDayChanged(value: String) = _form.update { it.copy(day = value.filter(Char::isDigit).take(2)) }
    fun onMonthChanged(value: String) = _form.update { it.copy(month = value.filter(Char::isDigit).take(2)) }
    fun onYearChanged(value: String) = _form.update { it.copy(year = value.filter(Char::isDigit).take(4)) }

    fun onVisibilityChanged(value: SocialVisibility) = _form.update { it.copy(visibility = value) }

    fun submit(onSuccess: (UserProfileResponse) -> Unit) {
        val current = _form.value
        if (!isFormValid(current)) {
            _state.value = SocialSubmitState.Error("Formulaire invalide")
            return
        }
        if (_handleCheck.value == HandleCheckState.Taken || _handleCheck.value == HandleCheckState.Invalid) {
            _state.value = SocialSubmitState.Error("Pseudo invalide ou deja pris")
            return
        }
        val dateIso =
            if (current.dateLocked) null // ne pas envoyer pour ne pas modifier
            else buildIsoDate(current.day, current.month, current.year)
                ?: run {
                    _state.value = SocialSubmitState.Error("Date de naissance invalide")
                    return
                }

        _state.value = SocialSubmitState.Saving
        scope.launch {
            val req = UpdateSocialProfileRequest(
                handle = current.handle,
                bio = current.bio.trim().ifEmpty { null },
                dateNaissance = dateIso,
                socialVisibility = current.visibility.name,
            )
            when (val r = userRepository.updateSocialProfile(req)) {
                is AppResult.Success -> {
                    _state.value = SocialSubmitState.Success
                    onSuccess(r.data)
                }
                is AppResult.Error -> {
                    _state.value = SocialSubmitState.Error(r.message ?: "Erreur lors de l'enregistrement")
                }
            }
        }
    }

    fun resetState() {
        _state.value = SocialSubmitState.Idle
    }

    private fun isFormValid(form: SocialFormState): Boolean {
        if (!HANDLE_REGEX.matches(form.handle)) return false
        if (form.dateLocked) return true
        return buildIsoDate(form.day, form.month, form.year) != null
    }

    companion object {
        private val HANDLE_REGEX = Regex("^[a-zA-Z0-9_]{3,30}$")

        /**
         * Construit une date ISO (YYYY-MM-DD) si les composants sont valides
         * et que la personne a au moins 13 ans.
         */
        fun buildIsoDate(day: String, month: String, year: String): String? {
            val d = day.toIntOrNull() ?: return null
            val m = month.toIntOrNull() ?: return null
            val y = year.toIntOrNull() ?: return null
            if (d !in 1..31 || m !in 1..12 || y !in 1900..2100) return null
            // Verif basique du nombre de jours par mois
            val maxDay = when (m) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(y)) 29 else 28
                else -> 31
            }
            if (d > maxDay) return null
            return "${y.toString().padStart(4, '0')}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
        }

        private fun isLeapYear(year: Int): Boolean =
            (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

        private fun parseDate(iso: String?): Triple<String, String, String> {
            if (iso == null) return Triple("", "", "")
            val parts = iso.split("-")
            if (parts.size != 3) return Triple("", "", "")
            return Triple(parts[2], parts[1], parts[0])
        }
    }
}

data class SocialFormState(
    val handle: String = "",
    val bio: String = "",
    val day: String = "",
    val month: String = "",
    val year: String = "",
    val visibility: SocialVisibility = SocialVisibility.PRIVATE,
    val dateLocked: Boolean = false,
)

enum class HandleCheckState {
    Idle,
    Checking,
    Available,
    Taken,
    Invalid,

    /** Le handle actuel est celui du user lui-meme — pas besoin de re-verifier. */
    Owned,
}

sealed class SocialSubmitState {
    data object Idle : SocialSubmitState()
    data object Saving : SocialSubmitState()
    data object Success : SocialSubmitState()
    data class Error(val message: String) : SocialSubmitState()
}
