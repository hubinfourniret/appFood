package com.appfood.shared.ui.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.SocialVisibility
import com.appfood.shared.ui.Strings

/**
 * TACHE-600 : ecran reutilisable pour onboarding social ET settings social.
 *
 * @param mode Onboarding ou Settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialProfileScreen(
    viewModel: SocialProfileViewModel,
    mode: SocialProfileMode,
    onSaved: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
) {
    val form by viewModel.form.collectAsState()
    val handleCheck by viewModel.handleCheck.collectAsState()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when (val s = state) {
            is SocialSubmitState.Success -> {
                snackbarHostState.showSnackbar(Strings.SOCIAL_SETTINGS_SAVED)
                onSaved()
                viewModel.resetState()
            }
            is SocialSubmitState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (mode) {
                            SocialProfileMode.Onboarding -> Strings.SOCIAL_ONBOARDING_TITLE
                            SocialProfileMode.Settings -> Strings.SOCIAL_SETTINGS_TITLE
                        }
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        TextButton(onClick = onNavigateBack) { Text(Strings.ICON_BACK) }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (mode == SocialProfileMode.Onboarding) {
                Text(
                    text = Strings.SOCIAL_ONBOARDING_INTRO,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HandleField(
                value = form.handle,
                handleCheck = handleCheck,
                onValueChange = viewModel::onHandleChanged,
            )

            OutlinedTextField(
                value = form.bio,
                onValueChange = viewModel::onBioChanged,
                label = { Text(Strings.SOCIAL_BIO_LABEL) },
                supportingText = { Text("${form.bio.length}/280 — ${Strings.SOCIAL_BIO_HELPER}") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            DateNaissanceField(
                day = form.day,
                month = form.month,
                year = form.year,
                locked = form.dateLocked,
                onDayChanged = viewModel::onDayChanged,
                onMonthChanged = viewModel::onMonthChanged,
                onYearChanged = viewModel::onYearChanged,
            )

            VisibilitySection(
                selected = form.visibility,
                onSelect = viewModel::onVisibilityChanged,
            )

            if (mode == SocialProfileMode.Onboarding) {
                Text(
                    text = Strings.SOCIAL_NOTE_AGE_GATE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val saving = state is SocialSubmitState.Saving
            val canSubmit = !saving &&
                handleCheck != HandleCheckState.Taken &&
                handleCheck != HandleCheckState.Invalid &&
                handleCheck != HandleCheckState.Checking &&
                form.handle.isNotBlank() &&
                (form.dateLocked || (form.day.isNotBlank() && form.month.isNotBlank() && form.year.isNotBlank()))

            Button(
                onClick = { viewModel.submit { onSaved() } },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (saving) Strings.SOCIAL_SUBMIT_SAVING
                    else when (mode) {
                        SocialProfileMode.Onboarding -> Strings.SOCIAL_SUBMIT
                        SocialProfileMode.Settings -> Strings.SOCIAL_SETTINGS_SAVE
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HandleField(
    value: String,
    handleCheck: HandleCheckState,
    onValueChange: (String) -> Unit,
) {
    val supportingText = when (handleCheck) {
        HandleCheckState.Idle -> Strings.SOCIAL_HANDLE_HELPER
        HandleCheckState.Checking -> Strings.SOCIAL_HANDLE_CHECKING
        HandleCheckState.Available -> Strings.SOCIAL_HANDLE_AVAILABLE
        HandleCheckState.Taken -> Strings.SOCIAL_HANDLE_TAKEN
        HandleCheckState.Invalid -> Strings.SOCIAL_HANDLE_INVALID
        HandleCheckState.Owned -> Strings.SOCIAL_HANDLE_HELPER
    }
    val isError = handleCheck == HandleCheckState.Taken || handleCheck == HandleCheckState.Invalid
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(Strings.SOCIAL_HANDLE_LABEL) },
        supportingText = { Text(supportingText) },
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DateNaissanceField(
    day: String,
    month: String,
    year: String,
    locked: Boolean,
    onDayChanged: (String) -> Unit,
    onMonthChanged: (String) -> Unit,
    onYearChanged: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = Strings.SOCIAL_DATE_NAISSANCE_LABEL,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = day,
                onValueChange = onDayChanged,
                label = { Text(Strings.SOCIAL_DATE_DAY) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !locked,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = month,
                onValueChange = onMonthChanged,
                label = { Text(Strings.SOCIAL_DATE_MONTH) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !locked,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = year,
                onValueChange = onYearChanged,
                label = { Text(Strings.SOCIAL_DATE_YEAR) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !locked,
                modifier = Modifier.weight(1.4f),
            )
        }
        Text(
            text = if (locked) Strings.SOCIAL_SETTINGS_DATE_LOCKED else Strings.SOCIAL_DATE_NAISSANCE_HELPER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VisibilitySection(
    selected: SocialVisibility,
    onSelect: (SocialVisibility) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = Strings.SOCIAL_VISIBILITY_LABEL,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        VisibilityOption(
            selected = selected == SocialVisibility.PRIVATE,
            label = Strings.SOCIAL_VISIBILITY_PRIVATE,
            description = Strings.SOCIAL_VISIBILITY_PRIVATE_DESC,
            onSelect = { onSelect(SocialVisibility.PRIVATE) },
        )
        VisibilityOption(
            selected = selected == SocialVisibility.FRIENDS,
            label = Strings.SOCIAL_VISIBILITY_FRIENDS,
            description = Strings.SOCIAL_VISIBILITY_FRIENDS_DESC,
            onSelect = { onSelect(SocialVisibility.FRIENDS) },
        )
        VisibilityOption(
            selected = selected == SocialVisibility.PUBLIC,
            label = Strings.SOCIAL_VISIBILITY_PUBLIC,
            description = Strings.SOCIAL_VISIBILITY_PUBLIC_DESC,
            onSelect = { onSelect(SocialVisibility.PUBLIC) },
        )
    }
}

@Composable
private fun VisibilityOption(
    selected: Boolean,
    label: String,
    description: String,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

enum class SocialProfileMode {
    Onboarding,
    Settings,
}
