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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.MealType
import com.appfood.shared.ui.common.IncompleteProfileBanner
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.DashboardLoadingSkeleton
import com.appfood.shared.ui.common.EmptyDashboardState
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.NetworkErrorMessage
import com.appfood.shared.ui.common.NutrimentProgressBar
import com.appfood.shared.ui.common.ServerErrorMessage

/**
 * Dashboard screen (DASHBOARD-01).
 * Connected composable that delegates to pure content composable.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    hydratationViewModel: com.appfood.shared.ui.hydratation.HydratationViewModel,
    onNavigateToAddEntry: () -> Unit,
    onNavigateToQuotaManagement: () -> Unit,
    onNavigateToRecommandations: () -> Unit,
    onNavigateToHydratation: () -> Unit,
    onNavigateToWeeklyDashboard: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val hydratationState by hydratationViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
        hydratationViewModel.init()
    }

    DashboardContent(
        state = state,
        hydratationState = hydratationState,
        onRetry = viewModel::retry,
        onAddMeal = onNavigateToAddEntry,
        onManageQuotas = onNavigateToQuotaManagement,
        onSeeRecommandations = onNavigateToRecommandations,
        onAddGlass = hydratationViewModel::addGlass,
        onAddBottle = hydratationViewModel::addBottle,
        onNavigateToHydratation = onNavigateToHydratation,
        onNavigateToWeeklyDashboard = onNavigateToWeeklyDashboard,
        onNavigateToOnboarding = onNavigateToOnboarding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: DashboardState,
    hydratationState: com.appfood.shared.ui.hydratation.HydratationState,
    onRetry: () -> Unit,
    onAddMeal: () -> Unit,
    onManageQuotas: () -> Unit,
    onSeeRecommandations: () -> Unit,
    onAddGlass: () -> Unit,
    onAddBottle: () -> Unit,
    onNavigateToHydratation: () -> Unit,
    onNavigateToWeeklyDashboard: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.DASHBOARD_TITLE) },
            )
        },
    ) { innerPadding ->
        when (state) {
            is DashboardState.Loading -> {
                DashboardLoadingSkeleton(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is DashboardState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is DashboardState.Success -> {
                val isEmpty = !state.hasJournalEntries

                if (isEmpty) {
                    EmptyDashboardState(
                        onAddFirstMeal = onAddMeal,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    DashboardSuccessContent(
                        state = state,
                        onAddMeal = onAddMeal,
                        onManageQuotas = onManageQuotas,
                        onSeeRecommandations = onSeeRecommandations,
                        onNavigateToOnboarding = onNavigateToOnboarding,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSuccessContent(
    state: DashboardState.Success,
    onAddMeal: () -> Unit,
    onManageQuotas: () -> Unit,
    onSeeRecommandations: () -> Unit,
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
        // PROFIL-01 — Bandeau rappel profil incomplet
        if (!state.onboardingComplete) {
            IncompleteProfileBanner(
                onCompleteProfile = onNavigateToOnboarding,
            )
        }

        // Date
        Text(
            text = state.date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Calories summary card
        CaloriesSummaryCard(
            caloriesConsommees = state.caloriesConsommees,
            caloriesCible = state.caloriesCible,
        )

        // Nutriment sections grouped by category
        val grouped = state.quotasStatus.groupBy { it.categorie }

        grouped[NutrimentCategorie.MACRO]?.let { macros ->
            NutrimentSection(
                title = Strings.DASHBOARD_MACROS_TITLE,
                quotas = macros,
                onManageQuotas = onManageQuotas,
            )
        }

        grouped[NutrimentCategorie.VITAMINE]?.let { vitamines ->
            NutrimentSection(
                title = Strings.DASHBOARD_VITAMINES_TITLE,
                quotas = vitamines,
                onManageQuotas = onManageQuotas,
            )
        }

        grouped[NutrimentCategorie.MINERAL]?.let { mineraux ->
            NutrimentSection(
                title = Strings.DASHBOARD_MINERAUX_TITLE,
                quotas = mineraux,
                onManageQuotas = onManageQuotas,
            )
        }

        grouped[NutrimentCategorie.ACIDE_GRAS]?.let { acidesGras ->
            NutrimentSection(
                title = Strings.DASHBOARD_ACIDES_GRAS_TITLE,
                quotas = acidesGras,
                onManageQuotas = onManageQuotas,
            )
        }

        // Meals summary
        MealsSummarySection(
            repas = state.repas,
        )

        // See recommendations button
        TextButton(
            onClick = onSeeRecommandations,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(Strings.DASHBOARD_SEE_RECOMMENDATIONS)
        }

        // Add meal button (CTA)
        Button(
            onClick = onAddMeal,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(Strings.DASHBOARD_ADD_MEAL)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CaloriesSummaryCard(
    caloriesConsommees: Double,
    caloriesCible: Double,
) {
    val percentage = if (caloriesCible > 0) caloriesConsommees / caloriesCible else 0.0

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

@Composable
private fun MealsSummarySection(
    repas: Map<MealType, Double>,
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
            Text(
                text = Strings.DASHBOARD_REPAS_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            MealType.entries.forEach { mealType ->
                val calories = repas[mealType] ?: 0.0
                MealRow(
                    mealType = mealType,
                    calories = calories,
                )
            }
        }
    }
}

@Composable
private fun MealRow(
    mealType: MealType,
    calories: Double,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mealTypeEmoji(mealType),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = mealTypeLabel(mealType),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = if (calories > 0) {
                "${formatCalories(calories)} kcal"
            } else {
                Strings.DASHBOARD_NO_ENTRY
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (calories > 0) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> Strings.JOURNAL_MEAL_BREAKFAST
    MealType.DEJEUNER -> Strings.JOURNAL_MEAL_LUNCH
    MealType.DINER -> Strings.JOURNAL_MEAL_DINNER
    MealType.COLLATION -> Strings.JOURNAL_MEAL_SNACK
}

private fun mealTypeEmoji(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> "\uD83C\uDF05"
    MealType.DEJEUNER -> "\uD83C\uDF7D\uFE0F"
    MealType.DINER -> "\uD83C\uDF19"
    MealType.COLLATION -> "\uD83C\uDF4E"
}

private fun formatCalories(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        val rounded = kotlin.math.round(value).toLong()
        rounded.toString()
    }
}
