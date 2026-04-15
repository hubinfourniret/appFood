package com.appfood.shared.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.request.LoginRequest
import com.appfood.shared.api.request.RegisterRequest
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.ui.Strings
import com.appfood.shared.util.AppResult
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel shared across auth screens (Login, Register, ForgotPassword).
 * Calls UserRepository directly for authentication operations.
 */
class AuthViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // Login form fields
    private val _loginEmail = MutableStateFlow("")
    val loginEmail: StateFlow<String> = _loginEmail.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    // Register form fields
    private val _registerEmail = MutableStateFlow("")
    val registerEmail: StateFlow<String> = _registerEmail.asStateFlow()

    private val _registerPassword = MutableStateFlow("")
    val registerPassword: StateFlow<String> = _registerPassword.asStateFlow()

    private val _registerConfirmPassword = MutableStateFlow("")
    val registerConfirmPassword: StateFlow<String> = _registerConfirmPassword.asStateFlow()

    // Forgot password form field
    private val _forgotPasswordEmail = MutableStateFlow("")
    val forgotPasswordEmail: StateFlow<String> = _forgotPasswordEmail.asStateFlow()

    // Validation errors
    private val _loginEmailError = MutableStateFlow<String?>(null)
    val loginEmailError: StateFlow<String?> = _loginEmailError.asStateFlow()

    private val _loginPasswordError = MutableStateFlow<String?>(null)
    val loginPasswordError: StateFlow<String?> = _loginPasswordError.asStateFlow()

    private val _registerEmailError = MutableStateFlow<String?>(null)
    val registerEmailError: StateFlow<String?> = _registerEmailError.asStateFlow()

    private val _registerPasswordError = MutableStateFlow<String?>(null)
    val registerPasswordError: StateFlow<String?> = _registerPasswordError.asStateFlow()

    private val _registerConfirmPasswordError = MutableStateFlow<String?>(null)
    val registerConfirmPasswordError: StateFlow<String?> = _registerConfirmPasswordError.asStateFlow()

    private val _forgotPasswordEmailError = MutableStateFlow<String?>(null)
    val forgotPasswordEmailError: StateFlow<String?> = _forgotPasswordEmailError.asStateFlow()

    // --- Login form ---

    fun onLoginEmailChanged(value: String) {
        _loginEmail.value = value
        _loginEmailError.value = null
    }

    fun onLoginPasswordChanged(value: String) {
        _loginPassword.value = value
        _loginPasswordError.value = null
    }

    fun onLogin() {
        if (!validateLoginForm()) return

        _state.value = AuthState.Loading
        viewModelScope.launch {
            val firebaseToken = try {
                Firebase.auth.signInWithEmailAndPassword(_loginEmail.value, _loginPassword.value)
                Firebase.auth.currentUser?.getIdToken(false)
            } catch (e: Exception) {
                _state.value = AuthState.Error(mapFirebaseError(e))
                return@launch
            }
            if (firebaseToken.isNullOrBlank()) {
                _state.value = AuthState.Error(Strings.AUTH_ERROR_GENERIC)
                return@launch
            }

            val result = userRepository.login(LoginRequest(firebaseToken = firebaseToken))
            when (result) {
                is AppResult.Success -> {
                    val needsOnboarding = !result.data.user.onboardingComplete
                    _state.value = AuthState.Success(needsOnboarding = needsOnboarding)
                }
                is AppResult.Error -> {
                    _state.value = AuthState.Error(result.message)
                }
            }
        }
    }

    private fun validateLoginForm(): Boolean {
        var valid = true
        if (_loginEmail.value.isBlank()) {
            _loginEmailError.value = Strings.VALIDATION_FIELD_REQUIRED
            valid = false
        } else if (!isValidEmail(_loginEmail.value)) {
            _loginEmailError.value = Strings.VALIDATION_EMAIL_INVALID
            valid = false
        }
        if (_loginPassword.value.isBlank()) {
            _loginPasswordError.value = Strings.VALIDATION_FIELD_REQUIRED
            valid = false
        }
        return valid
    }

    // --- Register form ---

    fun onRegisterEmailChanged(value: String) {
        _registerEmail.value = value
        _registerEmailError.value = null
    }

    fun onRegisterPasswordChanged(value: String) {
        _registerPassword.value = value
        _registerPasswordError.value = null
    }

    fun onRegisterConfirmPasswordChanged(value: String) {
        _registerConfirmPassword.value = value
        _registerConfirmPasswordError.value = null
    }

    fun onRegister() {
        if (!validateRegisterForm()) return

        _state.value = AuthState.Loading
        viewModelScope.launch {
            val firebaseToken = try {
                Firebase.auth.createUserWithEmailAndPassword(_registerEmail.value, _registerPassword.value)
                Firebase.auth.currentUser?.getIdToken(false)
            } catch (e: Exception) {
                _state.value = AuthState.Error(mapFirebaseError(e))
                return@launch
            }
            if (firebaseToken.isNullOrBlank()) {
                _state.value = AuthState.Error(Strings.AUTH_ERROR_GENERIC)
                return@launch
            }

            val result = userRepository.register(
                RegisterRequest(
                    firebaseToken = firebaseToken,
                    email = _registerEmail.value,
                    nom = null,
                    prenom = null,
                )
            )
            when (result) {
                is AppResult.Success -> {
                    try {
                        Firebase.auth.currentUser?.sendEmailVerification()
                    } catch (_: Exception) {
                        println("WARNING: Echec envoi email de verification")
                    }
                    _state.value = AuthState.Success(needsOnboarding = true)
                }
                is AppResult.Error -> {
                    _state.value = AuthState.Error(result.message)
                }
            }
        }
    }

    private fun mapFirebaseError(e: Exception): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("password is invalid", ignoreCase = true) ||
                msg.contains("INVALID_PASSWORD", ignoreCase = true) ||
                msg.contains("invalid-credential", ignoreCase = true) -> Strings.AUTH_ERROR_INVALID_CREDENTIALS
            msg.contains("no user record", ignoreCase = true) ||
                msg.contains("USER_NOT_FOUND", ignoreCase = true) -> Strings.AUTH_ERROR_USER_NOT_FOUND
            msg.contains("EMAIL_EXISTS", ignoreCase = true) ||
                msg.contains("email address is already in use", ignoreCase = true) -> Strings.AUTH_ERROR_EMAIL_IN_USE
            msg.contains("WEAK_PASSWORD", ignoreCase = true) ||
                msg.contains("password should be at least", ignoreCase = true) -> Strings.AUTH_ERROR_WEAK_PASSWORD
            msg.contains("network", ignoreCase = true) -> Strings.AUTH_ERROR_NETWORK
            else -> e.message ?: Strings.AUTH_ERROR_GENERIC
        }
    }

    private fun validateRegisterForm(): Boolean {
        var valid = true
        if (_registerEmail.value.isBlank()) {
            _registerEmailError.value = Strings.VALIDATION_FIELD_REQUIRED
            valid = false
        } else if (!isValidEmail(_registerEmail.value)) {
            _registerEmailError.value = Strings.VALIDATION_EMAIL_INVALID
            valid = false
        }
        if (_registerPassword.value.length < MIN_PASSWORD_LENGTH) {
            _registerPasswordError.value = Strings.VALIDATION_PASSWORD_TOO_SHORT
            valid = false
        }
        if (_registerConfirmPassword.value != _registerPassword.value) {
            _registerConfirmPasswordError.value = Strings.REGISTER_ERROR_PASSWORDS_MISMATCH
            valid = false
        }
        return valid
    }

    // --- Forgot password ---

    fun onForgotPasswordEmailChanged(value: String) {
        _forgotPasswordEmail.value = value
        _forgotPasswordEmailError.value = null
    }

    fun onSendResetLink() {
        if (_forgotPasswordEmail.value.isBlank()) {
            _forgotPasswordEmailError.value = Strings.VALIDATION_FIELD_REQUIRED
            return
        }
        if (!isValidEmail(_forgotPasswordEmail.value)) {
            _forgotPasswordEmailError.value = Strings.VALIDATION_EMAIL_INVALID
            return
        }

        _state.value = AuthState.Loading
        viewModelScope.launch {
            try {
                Firebase.auth.sendPasswordResetEmail(_forgotPasswordEmail.value)
                _state.value = AuthState.ResetEmailSent
            } catch (e: Exception) {
                _state.value = AuthState.Error(
                    e.message ?: "Erreur lors de l'envoi de l'e-mail de reinitialisation"
                )
            }
        }
    }

    // --- Social sign-in ---

    fun onGoogleSignIn() {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            // TODO: Call googleSignInUseCase when created by SHARED agent
            // The flow is: Firebase SDK handles OAuth, returns token
            // Then we call our backend to register/login the user
            _state.value = AuthState.Success(needsOnboarding = true)
        }
    }

    fun onAppleSignIn() {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            // TODO: Call appleSignInUseCase when created by SHARED agent
            _state.value = AuthState.Success(needsOnboarding = true)
        }
    }

    // --- Delete account ---

    fun onDeleteAccount() {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            when (val result = userRepository.deleteAccount()) {
                is AppResult.Success -> {
                    _state.value = AuthState.AccountDeleted
                }
                is AppResult.Error -> {
                    _state.value = AuthState.Error(result.message)
                }
            }
        }
    }

    // --- Logout ---

    fun onLogout() {
        viewModelScope.launch {
            when (val result = userRepository.logout()) {
                is AppResult.Success -> {
                    _state.value = AuthState.Idle
                }
                is AppResult.Error -> {
                    // Meme en cas d'erreur, on deconnecte localement
                    _state.value = AuthState.Idle
                }
            }
        }
    }

    // --- Utilities ---

    fun resetState() {
        _state.value = AuthState.Idle
    }

    fun clearLoginForm() {
        _loginEmail.value = ""
        _loginPassword.value = ""
        _loginEmailError.value = null
        _loginPasswordError.value = null
    }

    fun clearRegisterForm() {
        _registerEmail.value = ""
        _registerPassword.value = ""
        _registerConfirmPassword.value = ""
        _registerEmailError.value = null
        _registerPasswordError.value = null
        _registerConfirmPasswordError.value = null
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8

        fun isValidEmail(email: String): Boolean {
            // Simple email validation — checks basic pattern
            return email.contains("@") && email.contains(".") && email.length >= 5
        }
    }
}

/**
 * Sealed interface representing all possible auth states.
 */
sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val needsOnboarding: Boolean) : AuthState
    data class Error(val message: String) : AuthState
    data object ResetEmailSent : AuthState
    data object AccountDeleted : AuthState
}
