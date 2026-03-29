package com.appfood.shared.ui.poids

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton
import com.appfood.shared.ui.common.SimpleLineChart

/**
 * Weight tracking screen (POIDS-01).
 * Connected composable with weight entry form and history chart.
 */
@Composable
fun PoidsScreen(
    viewModel: PoidsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val inputPoids by viewModel.inputPoids.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    PoidsContent(
        state = state,
        inputPoids = inputPoids,
        isSaving = isSaving,
        saveMessage = saveMessage,
        selectedPeriod = selectedPeriod,
        onPoidsInputChanged = viewModel::onPoidsInputChanged,
        onSavePoids = viewModel::onSavePoids,
        onPeriodChanged = viewModel::onPeriodChanged,
        onClearMessage = viewModel::clearSaveMessage,
        onRetry = viewModel::retry,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoidsContent(
    state: PoidsState,
    inputPoids: String,
    isSaving: Boolean,
    saveMessage: String?,
    selectedPeriod: PoidsPeriod,
    onPoidsInputChanged: (String) -> Unit,
    onSavePoids: () -> Unit,
    onPeriodChanged: (PoidsPeriod) -> Unit,
    onClearMessage: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.SCREEN_POIDS) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Weight input card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = Strings.POIDS_SAISIE_TITLE,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        OutlinedTextField(
                            value = inputPoids,
                            onValueChange = onPoidsInputChanged,
                            label = { Text(Strings.POIDS_LABEL) },
                            suffix = { Text(Strings.POIDS_UNIT) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.width(160.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onSavePoids,
                            enabled = !isSaving && inputPoids.toDoubleOrNull() != null,
                        ) {
                            Text(Strings.POIDS_SAVE_BUTTON)
                        }
                    }
                }
            }

            // Stats card (current, min, max)
            if (state is PoidsState.Success) {
                PoidsStatsCard(state = state)
            }

            // Period selector
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodChanged = onPeriodChanged,
            )

            // Chart / content
            when (state) {
                is PoidsState.Loading -> {
                    LoadingSkeleton(lines = 4)
                }

                is PoidsState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = onRetry,
                    )
                }

                is PoidsState.Success -> {
                    if (state.entries.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            SimpleLineChart(
                                dataPoints = state.entries.map { it.date to it.poids },
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    } else {
                        Text(
                            text = Strings.POIDS_NO_DATA,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PoidsStatsCard(state: PoidsState.Success) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                label = Strings.POIDS_CURRENT,
                value = state.poidsCourant?.let { "${it}${Strings.POIDS_UNIT}" } ?: "-",
            )
            StatItem(
                label = Strings.POIDS_MIN,
                value = state.poidsMin?.let { "${it}${Strings.POIDS_UNIT}" } ?: "-",
            )
            StatItem(
                label = Strings.POIDS_MAX,
                value = state.poidsMax?.let { "${it}${Strings.POIDS_UNIT}" } ?: "-",
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: PoidsPeriod,
    onPeriodChanged: (PoidsPeriod) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PoidsPeriod.entries.forEach { period ->
            val label = when (period) {
                PoidsPeriod.WEEK -> Strings.POIDS_PERIOD_WEEK
                PoidsPeriod.MONTH -> Strings.POIDS_PERIOD_MONTH
                PoidsPeriod.THREE_MONTHS -> Strings.POIDS_PERIOD_3MONTHS
                PoidsPeriod.SIX_MONTHS -> Strings.POIDS_PERIOD_6MONTHS
                PoidsPeriod.YEAR -> Strings.POIDS_PERIOD_YEAR
            }
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodChanged(period) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}
