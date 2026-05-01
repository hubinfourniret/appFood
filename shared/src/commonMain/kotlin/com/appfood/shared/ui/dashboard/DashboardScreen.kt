package com.appfood.shared.ui.dashboard

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.MealType
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.DashboardLoadingSkeleton
import com.appfood.shared.ui.common.EmptyDashboardState
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.IncompleteProfileBanner
import com.appfood.shared.ui.common.NutrimentProgressBar
import com.appfood.shared.ui.hydratation.HydratationEmbeddedContent
import com.appfood.shared.ui.hydratation.HydratationViewModel
import com.appfood.shared.ui.journal.EditEntryState
import com.appfood.shared.ui.journal.JournalViewModel

private enum class DashboardTab(val label: String) {
    QUOTAS(Strings.DASHBOARD_TAB_QUOTAS),
    REPAS(Strings.DASHBOARD_TAB_REPAS),
    EAU(Strings.DASHBOARD_TAB_EAU),
}

/**
 * Dashboard screen (DASHBOARD-01 + TACHE-514).
 * 3 onglets : Quotas / Repas / Eau, chacun avec acces a la vue hebdomadaire.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    hydratationViewModel: HydratationViewModel,
    journalViewModel: JournalViewModel,
    onNavigateToAddEntry: () -> Unit,
    onNavigateToQuotaManagement: () -> Unit,
    onNavigateToRecommandations: () -> Unit,
    onNavigateToHydratation: () -> Unit,
    onNavigateToWeeklyDashboard: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val showDeleteId by journalViewModel.showDeleteConfirmation.collectAsState()
    val editState by journalViewModel.editState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    // TACHE-514 : apres une suppression effective, recharger le dashboard
    LaunchedEffect(editState) {
        if (editState is EditEntryState.Deleted) {
            viewModel.loadDashboard()
            journalViewModel.resetEditState()
        }
    }

    DashboardContent(
        state = state,
        hydratationViewModel = hydratationViewModel,
        onRetry = viewModel::retry,
        onAddMeal = onNavigateToAddEntry,
        onManageQuotas = onNavigateToQuotaManagement,
        onSeeRecommandations = onNavigateToRecommandations,
        onNavigateToWeeklyDashboard = onNavigateToWeeklyDashboard,
        onNavigateToHydratationDetail = onNavigateToHydratation,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onRequestDeleteEntry = journalViewModel::onRequestDelete,
    )

    if (showDeleteId != null) {
        AlertDialog(
            onDismissRequest = journalViewModel::onCancelDelete,
            title = { Text(Strings.JOURNAL_DELETE_CONFIRM_TITLE) },
            text = { Text(Strings.JOURNAL_DELETE_CONFIRM_MESSAGE) },
            confirmButton = {
                Button(onClick = journalViewModel::onConfirmDelete) {
                    Text(Strings.JOURNAL_DELETE_CONFIRM)
                }
            },
            dismissButton = {
                TextButton(onClick = journalViewModel::onCancelDelete) {
                    Text(Strings.JOURNAL_DELETE_CANCEL)
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: DashboardState,
    hydratationViewModel: HydratationViewModel,
    onRetry: () -> Unit,
    onAddMeal: () -> Unit,
    onManageQuotas: () -> Unit,
    onSeeRecommandations: () -> Unit,
    onNavigateToWeeklyDashboard: () -> Unit,
    onNavigateToHydratationDetail: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onRequestDeleteEntry: (String) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.QUOTAS) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(Strings.DASHBOARD_TITLE) },
                )
                PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                    DashboardTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        when (state) {
            is DashboardState.Loading -> {
                DashboardLoadingSkeleton(modifier = Modifier.padding(innerPadding))
            }
            is DashboardState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is DashboardState.Success -> {
                if (!state.hasJournalEntries && selectedTab != DashboardTab.EAU) {
                    EmptyDashboardState(
                        onAddFirstMeal = onAddMeal,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    when (selectedTab) {
                        DashboardTab.QUOTAS -> QuotasTabContent(
                            state = state,
                            onManageQuotas = onManageQuotas,
                            onSeeRecommandations = onSeeRecommandations,
                            onNavigateToWeeklyDashboard = onNavigateToWeeklyDashboard,
                            onNavigateToOnboarding = onNavigateToOnboarding,
                            modifier = Modifier.padding(innerPadding),
                        )
                        DashboardTab.REPAS -> RepasTabContent(
                            state = state,
                            onSeeRecommandations = onSeeRecommandations,
                            onNavigateToWeeklyDashboard = onNavigateToWeeklyDashboard,
                            onRequestDeleteEntry = onRequestDeleteEntry,
                            modifier = Modifier.padding(innerPadding),
                        )
                        DashboardTab.EAU -> EauTabContent(
                            hydratationViewModel = hydratationViewModel,
                            onNavigateToHydratationDetail = onNavigateToHydratationDetail,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Onglet Quotas
// ---------------------------------------------------------------------------

@Composable
private fun QuotasTabContent(
    state: DashboardState.Success,
    onManageQuotas: () -> Unit,
    onSeeRecommandations: () -> Unit,
    onNavigateToWeeklyDashboard: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!state.onboardingComplete) {
            IncompleteProfileBanner(onCompleteProfile = onNavigateToOnboarding)
        }

        Text(
            text = state.date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CaloriesSummaryCard(
            caloriesConsommees = state.caloriesConsommees,
            caloriesCible = state.caloriesCible,
        )

        val grouped = state.quotasStatus.groupBy { it.categorie }
        grouped[NutrimentCategorie.MACRO]?.let {
            NutrimentSection(Strings.DASHBOARD_MACROS_TITLE, it, onManageQuotas)
        }
        grouped[NutrimentCategorie.VITAMINE]?.let {
            NutrimentSection(Strings.DASHBOARD_VITAMINES_TITLE, it, onManageQuotas)
        }
        grouped[NutrimentCategorie.MINERAL]?.let {
            NutrimentSection(Strings.DASHBOARD_MINERAUX_TITLE, it, onManageQuotas)
        }
        grouped[NutrimentCategorie.ACIDE_GRAS]?.let {
            NutrimentSection(Strings.DASHBOARD_ACIDES_GRAS_TITLE, it, onManageQuotas)
        }

        TextButton(onClick = onSeeRecommandations, modifier = Modifier.fillMaxWidth()) {
            Text(Strings.DASHBOARD_SEE_RECOMMENDATIONS)
        }
        TextButton(onClick = onNavigateToWeeklyDashboard, modifier = Modifier.fillMaxWidth()) {
            Text(Strings.DASHBOARD_SEE_WEEKLY)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// Onglet Repas
// ---------------------------------------------------------------------------

@Composable
private fun RepasTabContent(
    state: DashboardState.Success,
    onSeeRecommandations: () -> Unit,
    onNavigateToWeeklyDashboard: () -> Unit,
    onRequestDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = state.date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        MealType.entries.forEach { mealType ->
            val entries = state.entriesParRepas[mealType].orEmpty()
            val caloriesTotales = state.repas[mealType] ?: 0.0
            MealDetailCard(
                mealType = mealType,
                entries = entries,
                totalCalories = caloriesTotales,
                onRequestDeleteEntry = onRequestDeleteEntry,
            )
        }

        TextButton(onClick = onSeeRecommandations, modifier = Modifier.fillMaxWidth()) {
            Text(Strings.DASHBOARD_SEE_RECOMMENDATIONS)
        }
        TextButton(onClick = onNavigateToWeeklyDashboard, modifier = Modifier.fillMaxWidth()) {
            Text(Strings.DASHBOARD_SEE_WEEKLY)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MealDetailCard(
    mealType: MealType,
    entries: List<JournalEntryUiModel>,
    totalCalories: Double,
    onRequestDeleteEntry: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = mealTypeEmoji(mealType), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = mealTypeLabel(mealType),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = if (totalCalories > 0) "${formatCalories(totalCalories)} kcal" else Strings.DASHBOARD_NO_ENTRY,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (totalCalories > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (entries.isNotEmpty()) {
                HorizontalDivider()
                entries.forEach { entry ->
                    JournalEntryRow(
                        entry = entry,
                        onDelete = { onRequestDeleteEntry(entry.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalEntryRow(
    entry: JournalEntryUiModel,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.nom,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = formatEntryQuantity(entry),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${formatCalories(entry.calories)} kcal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        IconButton(onClick = onDelete) {
            Text(
                text = "🗑",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun formatEntryQuantity(entry: JournalEntryUiModel): String {
    return if (entry.isRecette && entry.nbPortions != null) {
        val p = entry.nbPortions
        val pStr = if (p == p.toLong().toDouble()) p.toLong().toString() else p.toString()
        "$pStr portion${if (p > 1) "s" else ""}"
    } else {
        "${formatCalories(entry.quantiteGrammes)} g"
    }
}

// ---------------------------------------------------------------------------
// Onglet Eau
// ---------------------------------------------------------------------------

@Composable
private fun EauTabContent(
    hydratationViewModel: HydratationViewModel,
    onNavigateToHydratationDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HydratationEmbeddedContent(
            viewModel = hydratationViewModel,
            modifier = Modifier.weight(1f),
        )
    }
}

// ---------------------------------------------------------------------------
// Composants partages
// ---------------------------------------------------------------------------

@Composable
private fun CaloriesSummaryCard(
    caloriesConsommees: Double,
    caloriesCible: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Strings.DASHBOARD_CALORIES_LABEL,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatCalories(caloriesConsommees)} / ${formatCalories(caloriesCible)} kcal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            NutrimentProgressBar(
                label = "",
                current = caloriesConsommees,
                target = caloriesCible,
                unit = "kcal",
            )
        }
    }
}

@Composable
private fun NutrimentSection(
    title: String,
    quotas: List<QuotaStatusUiModel>,
    onManageQuotas: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = Strings.QUOTAS_EDIT,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onManageQuotas),
                )
            }
            quotas.forEach { quota ->
                NutrimentProgressBar(
                    label = quota.nutriment,
                    current = quota.valeurConsommee,
                    target = quota.valeurCible,
                    unit = quota.unite,
                )
            }
        }
    }
}

private fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> Strings.JOURNAL_MEAL_BREAKFAST
    MealType.DEJEUNER -> Strings.JOURNAL_MEAL_LUNCH
    MealType.DINER -> Strings.JOURNAL_MEAL_DINNER
    MealType.COLLATION -> Strings.JOURNAL_MEAL_SNACK
}

private fun mealTypeEmoji(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> "🌅"
    MealType.DEJEUNER -> "🍽️"
    MealType.DINER -> "🌙"
    MealType.COLLATION -> "🍎"
}

private fun formatCalories(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        kotlin.math.round(value).toLong().toString()
    }
}
