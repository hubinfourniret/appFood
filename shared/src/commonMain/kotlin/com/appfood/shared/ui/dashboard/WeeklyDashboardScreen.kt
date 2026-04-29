package com.appfood.shared.ui.dashboard

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton
import com.appfood.shared.ui.common.NutrimentProgressBar

/**
 * Weekly dashboard screen (DASHBOARD-02).
 * Shows weekly averages, critical nutrients, and trends.
 */
@Composable
fun WeeklyDashboardScreen(
    viewModel: WeeklyDashboardViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    WeeklyDashboardContent(
        state = state,
        onPreviousWeek = viewModel::previousWeek,
        onNextWeek = viewModel::nextWeek,
        onRetry = viewModel::retry,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyDashboardContent(
    state: WeeklyState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.WEEKLY_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(Strings.ICON_BACK, style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            is WeeklyState.Loading -> {
                LoadingSkeleton(
                    lines = 8,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is WeeklyState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is WeeklyState.Success -> {
                WeeklySuccessContent(
                    state = state,
                    onPreviousWeek = onPreviousWeek,
                    onNextWeek = onNextWeek,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun WeeklySuccessContent(
    state: WeeklyState.Success,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Week navigation header
        WeekNavigationHeader(
            dateFrom = state.dateFrom,
            dateTo = state.dateTo,
            canGoNext = state.canGoNext,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek,
        )

        // Nutriment averages grouped by category
        val grouped = state.nutrimentAverages.groupBy { it.categorie }

        grouped[NutrimentCategorie.MACRO]?.let { macros ->
            NutrimentAverageSection(
                title = Strings.DASHBOARD_MACROS_TITLE,
                averages = macros,
            )
        }

        grouped[NutrimentCategorie.VITAMINE]?.let { vitamines ->
            NutrimentAverageSection(
                title = Strings.DASHBOARD_VITAMINES_TITLE,
                averages = vitamines,
            )
        }

        grouped[NutrimentCategorie.MINERAL]?.let { mineraux ->
            NutrimentAverageSection(
                title = Strings.DASHBOARD_MINERAUX_TITLE,
                averages = mineraux,
            )
        }

        grouped[NutrimentCategorie.ACIDE_GRAS]?.let { acidesGras ->
            NutrimentAverageSection(
                title = Strings.DASHBOARD_ACIDES_GRAS_TITLE,
                averages = acidesGras,
            )
        }

        // Hydration weekly bars
        HydratationWeeklySection(
            weeklyData = state.hydratationWeekly,
            objectifMl = state.hydratationObjectifMl,
        )

        // Critical nutrients
        if (state.criticalNutrients.isNotEmpty()) {
            TrendSection(
                title = Strings.WEEKLY_CRITICAL_NUTRIENTS,
                trends = state.criticalNutrients,
                trendColor = WeeklyRed,
            )
        }

        // Improvements
        if (state.improvements.isNotEmpty()) {
            TrendSection(
                title = Strings.WEEKLY_IMPROVEMENTS,
                trends = state.improvements,
                trendColor = WeeklyGreen,
            )
        }

        // Degradations
        if (state.degradations.isNotEmpty()) {
            TrendSection(
                title = Strings.WEEKLY_DEGRADATIONS,
                trends = state.degradations,
                trendColor = WeeklyOrange,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WeekNavigationHeader(
    dateFrom: String,
    dateTo: String,
    canGoNext: Boolean,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onPreviousWeek) {
            Text("${Strings.ICON_BACK} ${Strings.WEEKLY_PREVIOUS}")
        }
        Text(
            text = "$dateFrom — $dateTo",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        TextButton(
            onClick = onNextWeek,
            enabled = canGoNext,
        ) {
            Text("${Strings.WEEKLY_NEXT} \u2192")
        }
    }
}

@Composable
private fun NutrimentAverageSection(
    title: String,
    averages: List<NutrimentAverage>,
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
                    text = Strings.WEEKLY_AVERAGE,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            averages.forEach { avg ->
                NutrimentProgressBar(
                    label = avg.nutriment,
                    current = avg.moyenneConsommee,
                    target = avg.cible,
                    unit = avg.unite,
                )
            }
        }
    }
}

@Composable
private fun HydratationWeeklySection(
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
                text = Strings.HYDRA_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            val maxValue = maxOf(objectifMl, weeklyData.maxOrNull() ?: 0).coerceAtLeast(1)
            val barHeight = 80.dp

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
                    val barColor = if (reachedObjectif) WeeklyHydraGreen else WeeklyHydraBlue

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
private fun TrendSection(
    title: String,
    trends: List<NutrimentTrend>,
    trendColor: Color,
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = trendColor,
            )

            trends.forEach { trend ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = trend.nutriment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${formatTrendValue(trend.valeur)} / ${formatTrendValue(trend.cible)} ${trend.unite}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = trendColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

private fun formatTrendValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        val rounded = kotlin.math.round(value * 10.0) / 10.0
        rounded.toString()
    }
}

// Color constants
private val WeeklyRed = Color(0xFFF44336)
private val WeeklyGreen = Color(0xFF4CAF50)
private val WeeklyOrange = Color(0xFFFF9800)
private val WeeklyHydraBlue = Color(0xFF64B5F6)
private val WeeklyHydraGreen = Color(0xFF4CAF50)
