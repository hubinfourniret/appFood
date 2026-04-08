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
 *
 * The disclaimer is local-only (no backend endpoint).
 * Acceptance is tracked in-memory for the current session — the disclaimer
 * is shown once per onboarding flow (between Onboarding and Dashboard).
 * For future versions, persistence via local settings can be added.
 */
class DisclaimerViewModel : ViewModel() {

    private val _state = MutableStateFlow<DisclaimerState>(DisclaimerState.Pending)
    val state: StateFlow<DisclaimerState> = _state.asStateFlow()

    /**
     * Called when the user taps "J'ai compris" to accept the disclaimer.
     * Acceptance is immediate (no network call needed).
     */
    fun onAcceptDisclaimer() {
        _state.value = DisclaimerState.Accepted
    }
}

/**
 * Sealed interface representing disclaimer acceptance states.
 */
sealed interface DisclaimerState {
    /** Disclaimer not yet accepted — user must accept to continue. */
    data object Pending : DisclaimerState
    /** Saving acceptance in progress (kept for UI compatibility). */
    data object Saving : DisclaimerState
    /** Disclaimer accepted — user can proceed. */
    data object Accepted : DisclaimerState
    /** An error occurred (kept for UI compatibility). */
    data class Error(val message: String) : DisclaimerState
}
