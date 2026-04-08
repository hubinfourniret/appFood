package com.appfood.shared.ui.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.request.InitialConsentRequest
import com.appfood.shared.api.request.UpdateConsentRequest
import com.appfood.shared.data.remote.ConsentApi
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
    private val consentApi: ConsentApi,
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
            try {
                val response = consentApi.getConsents()
                response.data.forEach { consent ->
                    when (consent.type) {
                        "analytics" -> _analyticsEnabled.value = consent.accepte
                        "publicite" -> _advertisingEnabled.value = consent.accepte
                        "amelioration_service" -> _improvementEnabled.value = consent.accepte
                    }
                }
            } catch (_: Exception) {
                // Keep defaults on error — user can still toggle and save
            }
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

    /**
     * Save initial consents (first launch, after registration).
     * Calls POST /api/v1/consents/initial.
     */
    fun onConfirmChoices() {
        _state.value = ConsentState.Saving
        viewModelScope.launch {
            try {
                consentApi.saveInitialConsents(
                    InitialConsentRequest(
                        analytics = _analyticsEnabled.value,
                        publicite = _advertisingEnabled.value,
                        ameliorationService = _improvementEnabled.value,
                        versionPolitique = CURRENT_POLICY_VERSION,
                    ),
                )
                _state.value = ConsentState.Confirmed
            } catch (e: Exception) {
                _state.value = ConsentState.Error(e.message ?: "Erreur lors de la sauvegarde des consentements")
            }
        }
    }

    /**
     * Update a single consent type (from settings screen).
     * Calls PUT /api/v1/consents/{type}.
     */
    fun updateConsent(type: String, accepted: Boolean) {
        viewModelScope.launch {
            try {
                consentApi.updateConsent(
                    type = type,
                    request = UpdateConsentRequest(
                        accepte = accepted,
                        versionPolitique = CURRENT_POLICY_VERSION,
                    ),
                )
            } catch (_: Exception) {
                // Silently fail — toggle stays in UI state, will be retried next time
            }
        }
    }

    companion object {
        /** Current version of the privacy policy — bump when policy changes. */
        private const val CURRENT_POLICY_VERSION = "1.0"
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
