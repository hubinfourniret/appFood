package com.appfood.shared.ui.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the legal disclaimer screen (UX-05).
 * Tracks whether the user has accepted the disclaimer.
 */
class DisclaimerViewModel(
    // TODO: Inject ConsentRepository when created by SHARED agent
    // private val updateConsentUseCase: UpdateConsentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<DisclaimerState>(DisclaimerState.Pending)
    val state: StateFlow<DisclaimerState> = _state.asStateFlow()

    /**
     * Called when the user taps "J'ai compris" to accept the disclaimer.
     */
    fun onAcceptDisclaimer() {
        _state.value = DisclaimerState.Saving
        viewModelScope.launch {
            // TODO: Persist acceptance via ConsentRepository
            // val result = updateConsentUseCase(
            //     UpdateConsentRequest(
            //         consentType = ConsentType.DISCLAIMER_MEDICAL.name,
            //         accepted = true,
            //     )
            // )
            // when (result) {
            //     is AppResult.Success -> _state.value = DisclaimerState.Accepted
            //     is AppResult.Error -> _state.value = DisclaimerState.Error(result.message)
            // }

            // Stub: simulate success
            _state.value = DisclaimerState.Accepted
        }
    }
}

/**
 * Sealed interface representing disclaimer acceptance states.
 */
sealed interface DisclaimerState {
    /** Disclaimer not yet accepted — user must accept to continue. */
    data object Pending : DisclaimerState
    /** Saving acceptance in progress. */
    data object Saving : DisclaimerState
    /** Disclaimer accepted — user can proceed. */
    data object Accepted : DisclaimerState
    /** An error occurred while saving acceptance. */
    data class Error(val message: String) : DisclaimerState
}
