package com.appfood.shared.ui.quota

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.NutrimentType
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton

// Back arrow icon
private val BackArrowIcon: ImageVector by lazy {
    ImageVector.Builder("Back", 24.dp, 24.dp, 24f, 24f)
        .path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            close()
        }.build()
}

/**
 * Quota management screen (QUOTAS-02).
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun QuotaManagementScreen(
    viewModel: QuotaViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val editingNutriment by viewModel.editingNutriment.collectAsState()
    val editValue by viewModel.editValue.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadQuotas()
    }

    QuotaManagementContent(
        state = state,
        editingNutriment = editingNutriment,
        editValue = editValue,
        onStartEdit = viewModel::onStartEdit,
        onEditValueChanged = viewModel::onEditValueChanged,
        onCancelEdit = viewModel::onCancelEdit,
        onSaveEdit = viewModel::onSaveEdit,
        onResetQuota = viewModel::onResetQuota,
        onResetAllQuotas = viewModel::onResetAllQuotas,
        onRetry = viewModel::retry,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuotaManagementContent(
    state: QuotaManagementState,
    editingNutriment: NutrimentType?,
    editValue: String,
    onStartEdit: (NutrimentType) -> Unit,
    onEditValueChanged: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onResetQuota: (NutrimentType) -> Unit,
    onResetAllQuotas: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.QUOTAS_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackArrowIcon,
                            contentDescription = Strings.JOURNAL_BACK,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            is QuotaManagementState.Loading -> {
                LoadingSkeleton(
                    lines = 8,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is QuotaManagementState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is QuotaManagementState.Success -> {
                QuotaListContent(
                    quotas = state.quotas,
                    editingNutriment = editingNutriment,
                    editValue = editValue,
                    onStartEdit = onStartEdit,
                    onEditValueChanged = onEditValueChanged,
                    onCancelEdit = onCancelEdit,
                    onSaveEdit = onSaveEdit,
                    onResetQuota = onResetQuota,
                    onResetAllQuotas = onResetAllQuotas,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun QuotaListContent(
    quotas: List<QuotaUiModel>,
    editingNutriment: NutrimentType?,
    editValue: String,
    onStartEdit: (NutrimentType) -> Unit,
    onEditValueChanged: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onResetQuota: (NutrimentType) -> Unit,
    onResetAllQuotas: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasCustomQuotas = quotas.any { it.estPersonnalise }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Reset all button (only visible if any quota is customized)
        if (hasCustomQuotas) {
            OutlinedButton(
                onClick = onResetAllQuotas,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Strings.QUOTAS_RESET_ALL)
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Quota cards
        quotas.forEach { quota ->
            QuotaCard(
                quota = quota,
                isEditing = editingNutriment == quota.nutrimentType,
                editValue = editValue,
                onStartEdit = { onStartEdit(quota.nutrimentType) },
                onEditValueChanged = onEditValueChanged,
                onCancelEdit = onCancelEdit,
                onSaveEdit = onSaveEdit,
                onResetQuota = { onResetQuota(quota.nutrimentType) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuotaCard(
    quota: QuotaUiModel,
    isEditing: Boolean,
    editValue: String,
    onStartEdit: () -> Unit,
    onEditValueChanged: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onResetQuota: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (quota.estPersonnalise) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Header row: label + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = quota.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (quota.estPersonnalise) Strings.QUOTAS_CUSTOM else Strings.QUOTAS_CALCULATED,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (quota.estPersonnalise) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                // Edit mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = onEditValueChanged,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        suffix = { Text(quota.unite) },
                    )
                    Button(onClick = onSaveEdit) {
                        Text(Strings.QUOTAS_SAVE)
                    }
                    TextButton(onClick = onCancelEdit) {
                        Text(Strings.DELETE_ACCOUNT_CANCEL)
                    }
                }
            } else {
                // Display mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "${formatQuotaValue(quota.valeurCible)} ${quota.unite}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (quota.estPersonnalise) {
                            Text(
                                text = "${Strings.QUOTAS_CALCULATED} : ${formatQuotaValue(quota.valeurCalculee)} ${quota.unite}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row {
                        TextButton(onClick = onStartEdit) {
                            Text(Strings.QUOTAS_EDIT)
                        }
                        if (quota.estPersonnalise) {
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(onClick = onResetQuota) {
                                Text(
                                    text = Strings.QUOTAS_RESET,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatQuotaValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        val rounded = kotlin.math.round(value * 100.0) / 100.0
        rounded.toString()
    }
}
