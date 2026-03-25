package com.appfood.shared.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage

/**
 * Forgot password screen (AUTH-04).
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.forgotPasswordEmail.collectAsState()
    val emailError by viewModel.forgotPasswordEmailError.collectAsState()

    ForgotPasswordContent(
        email = email,
        emailError = emailError,
        isLoading = state is AuthState.Loading,
        isEmailSent = state is AuthState.ResetEmailSent,
        errorMessage = (state as? AuthState.Error)?.message,
        onEmailChanged = viewModel::onForgotPasswordEmailChanged,
        onSendResetLink = viewModel::onSendResetLink,
        onNavigateToLogin = {
            viewModel.resetState()
            onNavigateToLogin()
        },
        onDismissError = viewModel::resetState,
    )
}

/**
 * Pure forgot password content — testable and previewable.
 */
@Composable
private fun ForgotPasswordContent(
    email: String,
    emailError: String?,
    isLoading: Boolean,
    isEmailSent: Boolean,
    errorMessage: String?,
    onEmailChanged: (String) -> Unit,
    onSendResetLink: () -> Unit,
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
            text = Strings.FORGOT_PASSWORD_TITLE,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isEmailSent) {
            // Success state
            Text(
                text = Strings.FORGOT_PASSWORD_SUCCESS,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = Strings.FORGOT_PASSWORD_BACK_TO_LOGIN,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onNavigateToLogin),
            )
        } else {
            // Form state
            Text(
                text = Strings.FORGOT_PASSWORD_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
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
                label = { Text(Strings.FORGOT_PASSWORD_EMAIL_LABEL) },
                isError = emailError != null,
                supportingText = emailError?.let { error -> { Text(error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send button
            Button(
                onClick = onSendResetLink,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(24.dp),
                    )
                } else {
                    Text(Strings.FORGOT_PASSWORD_SEND_BUTTON)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to login link
            Text(
                text = Strings.FORGOT_PASSWORD_BACK_TO_LOGIN,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onNavigateToLogin),
            )
        }
    }
}
