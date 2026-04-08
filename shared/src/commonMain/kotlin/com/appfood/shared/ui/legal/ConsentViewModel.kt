package com.appfood.shared.ui.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the consent management screen (LEGAL-03).
 * Manages granular consent toggles: analytics, advertising, service improvement.
 * No tracking is activated without explicit user consent (RGPD / ePrivacy compliant).
 */
class ConsentViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val updateConsentUseCase: UpdateConsentUseCase,
    // private val getConsentsUseCase: GetConsentsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ConsentState>(ConsentState.Pending)
    val state: StateFlow<ConsentState> = _state.asStateFlow()

    // Granular consent toggles — all default to false (no tracking without explicit consent)
    private val _analyticsEnabled = MutableStateFlow(false)
    val analyticsEnabled: StateFlow<Boolean> = _analyticsEnabled.asStateFlow()

    private val _advertisingEnabled = MutableStateFlow(false)
    val advertisingEnabled: StateFlow<Boolean> = _advertisingEnabled.asStateFlow()

    private val _improvementEnabled = MutableStateFlow(false)
    val improvementEnabled: StateFlow<Boolean> = _improvementEnabled.asStateFlow()

    /**
     * Load existing consent preferences (used when opening consent settings).
     */
    fun loadExistingConsents() {
        viewModelScope.launch {
            // TODO: Load from ConsentRepository when created by SHARED agent
            // val result = getConsentsUseCase()
            // when (result) {
            //     is AppResult.Success -> {
            //         result.data.forEach { consent ->
            //             when (consent.type) {
            //                 ConsentType.ANALYTICS -> _analyticsEnabled.value = consent.accepted
            //                 ConsentType.ADVERTISING -> _advertisingEnabled.value = consent.accepted
            //                 ConsentType.SERVICE_IMPROVEMENT -> _improvementEnabled.value = consent.accepted
            //             }
            //         }
            //     }
            //     is AppResult.Error -> { /* Keep defaults */ }
            // }
        }
    }

    // --- Toggle setters ---

    fun onAnalyticsToggled(enabled: Boolean) {
        _analyticsEnabled.value = enabled
    }

    fun onAdvertisingToggled(enabled: Boolean) {
        _advertisingEnabled.value = enabled
    }

    fun onImprovementToggled(enabled: Boolean) {
        _improvementEnabled.value = enabled
    }

    // --- Bulk actions ---

    fun onAcceptAll() {
        _analyticsEnabled.value = true
        _advertisingEnabled.value = true
        _improvementEnabled.value = true
    }

    fun onRefuseAll() {
        _analyticsEnabled.value = false
        _advertisingEnabled.value = false
        _improvementEnabled.value = false
    }

    // --- Save ---

    fun onConfirmChoices() {
        _state.value = ConsentState.Saving
        viewModelScope.launch {
            // TODO: Persist consents via ConsentRepository when created by SHARED agent
            // val consents = listOf(
            //     UpdateConsentRequest(
            //         consentType = ConsentType.ANALYTICS.name,
            //         accepted = _analyticsEnabled.value,
            //     ),
            //     UpdateConsentRequest(
            //         consentType = ConsentType.ADVERTISING.name,
            //         accepted = _advertisingEnabled.value,
            //     ),
            //     UpdateConsentRequest(
            //         consentType = ConsentType.SERVICE_IMPROVEMENT.name,
            //         accepted = _improvementEnabled.value,
            //     ),
            // )
            // val result = updateConsentUseCase(InitialConsentRequest(consents = consents))
            // when (result) {
            //     is AppResult.Success -> _state.value = ConsentState.Confirmed
            //     is AppResult.Error -> _state.value = ConsentState.Error(result.message)
            // }

            // Stub: simulate success
            _state.value = ConsentState.Confirmed
        }
    }
}

/**
 * Sealed interface representing consent screen states.
 */
sealed interface ConsentState {
    /** User has not yet confirmed consent choices. */
    data object Pending : ConsentState
    /** Saving consent choices in progress. */
    data object Saving : ConsentState
    /** Consent choices confirmed and persisted. */
    data object Confirmed : ConsentState
    /** An error occurred while saving consent choices. */
    data class Error(val message: String) : ConsentState
}
