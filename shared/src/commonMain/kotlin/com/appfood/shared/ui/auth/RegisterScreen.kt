package com.appfood.shared.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage

/**
 * Register screen (AUTH-01).
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.registerEmail.collectAsState()
    val password by viewModel.registerPassword.collectAsState()
    val confirmPassword by viewModel.registerConfirmPassword.collectAsState()
    val emailError by viewModel.registerEmailError.collectAsState()
    val passwordError by viewModel.registerPasswordError.collectAsState()
    val confirmPasswordError by viewModel.registerConfirmPasswordError.collectAsState()

    // Handle navigation on success via LaunchedEffect to avoid recomposition loops
    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    RegisterContent(
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        emailError = emailError,
        passwordError = passwordError,
        confirmPasswordError = confirmPasswordError,
        isLoading = state is AuthState.Loading,
        errorMessage = (state as? AuthState.Error)?.message,
        onEmailChanged = viewModel::onRegisterEmailChanged,
        onPasswordChanged = viewModel::onRegisterPasswordChanged,
        onConfirmPasswordChanged = viewModel::onRegisterConfirmPasswordChanged,
        onRegister = viewModel::onRegister,
        onGoogleSignIn = viewModel::onGoogleSignIn,
        onAppleSignIn = viewModel::onAppleSignIn,
        onNavigateToLogin = onNavigateToLogin,
        onDismissError = viewModel::resetState,
    )
}

/**
 * Pure register content — testable and previewable.
 */
@Composable
private fun RegisterContent(
    email: String,
    password: String,
    confirmPassword: String,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onDismissError: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = Strings.APP_NAME,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Strings.REGISTER_TITLE,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        if (errorMessage != null) {
            ErrorMessage(
                message = errorMessage,
                onRetry = onDismissError,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text(Strings.REGISTER_EMAIL_LABEL) },
            isError = emailError != null,
            supportingText = emailError?.let { error -> { Text(error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text(Strings.REGISTER_PASSWORD_LABEL) },
            isError = passwordError != null,
            supportingText = passwordError?.let { error -> { Text(error) } },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Confirm password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = { Text(Strings.REGISTER_CONFIRM_PASSWORD_LABEL) },
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { error -> { Text(error) } },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Register button
        Button(
            onClick = onRegister,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp),
                )
            } else {
                Text(Strings.REGISTER_BUTTON)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = Strings.LOGIN_OR_SEPARATOR,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Google sign-in button (AUTH-03)
        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text(Strings.LOGIN_CONTINUE_WITH_GOOGLE)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apple sign-in button (AUTH-05)
        OutlinedButton(
            onClick = onAppleSignIn,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text(Strings.LOGIN_CONTINUE_WITH_APPLE)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigate to login
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = Strings.REGISTER_ALREADY_ACCOUNT + " ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = Strings.REGISTER_SIGN_IN,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onNavigateToLogin),
            )
        }
    }
}
