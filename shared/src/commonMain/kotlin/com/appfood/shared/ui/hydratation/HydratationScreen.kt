package com.appfood.shared.ui.hydratation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton

/**
 * Full hydration tracking screen (HYDRA-01).
 * Shows intake buttons, progress, and weekly history.
 */
@Composable
fun HydratationScreen(
    viewModel: HydratationViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val showCustomDialog by viewModel.showCustomDialog.collectAsState()
    val customInput by viewModel.customInput.collectAsState()
    val showObjectifDialog by viewModel.showObjectifDialog.collectAsState()
    val objectifInput by viewModel.objectifInput.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    HydratationContent(
        state = state,
        onAddGlass = viewModel::addGlass,
        onAddBottle = viewModel::addBottle,
        onShowCustom = viewModel::showCustomInput,
        onShowObjectifEditor = viewModel::showObjectifEditor,
        onResetObjectif = viewModel::resetObjectif,
        onRetry = viewModel::retry,
        onNavigateBack = onNavigateBack,
    )

    // Custom quantity dialog
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCustomDialog,
            title = { Text(Strings.HYDRA_ADD_CUSTOM) },
            text = {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = viewModel::onCustomInputChanged,
                    label = { Text(Strings.HYDRA_CUSTOM_LABEL) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = viewModel::addCustom) {
                    Text(Strings.QUOTAS_SAVE)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissCustomDialog) {
                    Text(Strings.DELETE_ACCOUNT_CANCEL)
                }
            },
        )
    }

    // Objectif editor dialog
    if (showObjectifDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissObjectifDialog,
            title = { Text(Strings.HYDRA_MODIFY_OBJECTIVE) },
            text = {
                OutlinedTextField(
                    value = objectifInput,
                    onValueChange = viewModel::onObjectifInputChanged,
                    label = { Text(Strings.HYDRA_CUSTOM_LABEL) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = viewModel::updateObjectif) {
                    Text(Strings.QUOTAS_SAVE)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissObjectifDialog) {
                    Text(Strings.DELETE_ACCOUNT_CANCEL)
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HydratationContent(
    state: HydratationState,
    onAddGlass: () -> Unit,
    onAddBottle: () -> Unit,
    onShowCustom: () -> Unit,
    onShowObjectifEditor: () -> Unit,
    onResetObjectif: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.HYDRA_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("\u2190", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            is HydratationState.Loading -> {
                LoadingSkeleton(
                    lines = 6,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is HydratationState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is HydratationState.Success -> {
                HydratationSuccessContent(
                    state = state,
                    onAddGlass = onAddGlass,
                    onAddBottle = onAddBottle,
                    onShowCustom = onShowCustom,
                    onShowObjectifEditor = onShowObjectifEditor,
                    onResetObjectif = onResetObjectif,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun HydratationSuccessContent(
    state: HydratationState.Success,
    onAddGlass: () -> Unit,
    onAddBottle: () -> Unit,
    onShowCustom: () -> Unit,
    onShowObjectifEditor: () -> Unit,
    onResetObjectif: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress section
        ProgressSection(
            quantiteMl = state.quantiteMl,
            objectifMl = state.objectifMl,
            pourcentage = state.pourcentage,
        )

        // Quick add buttons
        AddButtonsSection(
            onAddGlass = onAddGlass,
            onAddBottle = onAddBottle,
            onShowCustom = onShowCustom,
        )

        // Objectif management
        ObjectifSection(
            objectifMl = state.objectifMl,
            estPersonnalise = state.estPersonnalise,
            onModifyObjectif = onShowObjectifEditor,
            onResetObjectif = onResetObjectif,
        )

        // Weekly history
        WeeklyHistorySection(
            weeklyData = state.weeklyData,
            objectifMl = state.objectifMl,
        )

        // Today's entries
        if (state.entrees.isNotEmpty()) {
            TodayEntriesSection(entrees = state.entrees)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProgressSection(
    quantiteMl: Int,
    objectifMl: Int,
    pourcentage: Double,
) {
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
        ) {
            Text(
                text = Strings.hydraProgress(quantiteMl, objectifMl),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${pourcentage.toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (pourcentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = HydraProgressBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun AddButtonsSection(
    onAddGlass: () -> Unit,
    onAddBottle: () -> Unit,
    onShowCustom: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onAddGlass,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(Strings.HYDRA_ADD_GLASS)
                }
                Button(
                    onClick = onAddBottle,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(Strings.HYDRA_ADD_BOTTLE)
                }
            }
            OutlinedButton(
                onClick = onShowCustom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(Strings.HYDRA_ADD_CUSTOM)
            }
        }
    }
}

@Composable
private fun ObjectifSection(
    objectifMl: Int,
    estPersonnalise: Boolean,
    onModifyObjectif: () -> Unit,
    onResetObjectif: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${Strings.HYDRA_OBJECTIVE} : $objectifMl ml",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (estPersonnalise) {
                    Text(
                        text = Strings.QUOTAS_CUSTOM,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onModifyObjectif,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(Strings.HYDRA_MODIFY_OBJECTIVE)
                }
                if (estPersonnalise) {
                    TextButton(
                        onClick = onResetObjectif,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(Strings.HYDRA_RESET_OBJECTIVE)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyHistorySection(
    weeklyData: List<Int>,
    objectifMl: Int,
) {
    val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = Strings.HYDRA_WEEKLY_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            // Bar chart — 7 bars
            val maxValue = maxOf(objectifMl, weeklyData.maxOrNull() ?: 0).coerceAtLeast(1)
            val barHeight = 100.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight + 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                weeklyData.forEachIndexed { index, value ->
                    val fraction = (value.toFloat() / maxValue).coerceIn(0f, 1f)
                    val reachedObjectif = value >= objectifMl
                    val barColor = if (reachedObjectif) HydraBarGreen else HydraBarBlue

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeight * fraction.coerceAtLeast(0.02f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (index < dayLabels.size) dayLabels[index] else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayEntriesSection(
    entrees: List<HydratationEntree>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            entrees.forEach { entree ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = entree.heure,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${entree.quantiteMl} ml",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

// Color constants
private val HydraProgressBlue = Color(0xFF2196F3)
private val HydraBarBlue = Color(0xFF64B5F6)
private val HydraBarGreen = Color(0xFF4CAF50)
