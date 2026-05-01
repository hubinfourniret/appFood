package com.appfood.shared.ui.dashboard

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.appfood.shared.model.MealType
import com.appfood.shared.ui.Strings
import com.appfood.shared.ui.common.ErrorMessage
import com.appfood.shared.ui.common.LoadingSkeleton
import kotlinx.datetime.LocalDate

private val BackArrowIconWeekly: ImageVector by lazy {
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
 * TACHE-515 : ecran hebdo des repas saisis. 7 derniers jours, groupes par
 * jour puis par repas, avec liste des aliments/recettes consommes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRepasScreen(
    viewModel: WeeklyRepasViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.WEEKLY_REPAS_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = BackArrowIconWeekly,
                            contentDescription = Strings.JOURNAL_BACK,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val current = state) {
            is WeeklyRepasState.Loading -> {
                LoadingSkeleton(lines = 8, modifier = Modifier.padding(innerPadding))
            }
            is WeeklyRepasState.Error -> {
                ErrorMessage(
                    message = current.message,
                    onRetry = viewModel::load,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is WeeklyRepasState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // On affiche les jours du plus recent au plus ancien
                    current.days.reversed().forEach { day ->
                        DayCard(day = day)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DayCard(day: DaySummary) {
    val hasEntries = day.entriesByMeal.values.any { it.isNotEmpty() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDate(day.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (hasEntries) {
                        "${formatKcal(day.totalCalories)} kcal"
                    } else {
                        Strings.WEEKLY_REPAS_EMPTY_DAY
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasEntries) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (hasEntries) {
                MealType.entries.forEach { mealType ->
                    val entries = day.entriesByMeal[mealType].orEmpty()
                    if (entries.isNotEmpty()) {
                        HorizontalDivider()
                        MealBlock(mealType = mealType, entries = entries)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealBlock(mealType: MealType, entries: List<JournalEntryUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = mealEmoji(mealType), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = mealLabel(mealType),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        entries.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.nom,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = formatQuantity(entry),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${formatKcal(entry.calories)} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatQuantity(entry: JournalEntryUiModel): String {
    return if (entry.isRecette && entry.nbPortions != null) {
        val p = entry.nbPortions
        val pStr = if (p == p.toLong().toDouble()) p.toLong().toString() else p.toString()
        "$pStr portion${if (p > 1) "s" else ""}"
    } else {
        "${formatKcal(entry.quantiteGrammes)} g"
    }
}

private fun formatKcal(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString()
    else kotlin.math.round(value).toLong().toString()
}

private fun formatDate(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    val dayOfWeek = when (date.dayOfWeek.ordinal) {
        0 -> "Lun"
        1 -> "Mar"
        2 -> "Mer"
        3 -> "Jeu"
        4 -> "Ven"
        5 -> "Sam"
        else -> "Dim"
    }
    return "$dayOfWeek $day/$month"
}

private fun mealLabel(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> Strings.JOURNAL_MEAL_BREAKFAST
    MealType.DEJEUNER -> Strings.JOURNAL_MEAL_LUNCH
    MealType.DINER -> Strings.JOURNAL_MEAL_DINNER
    MealType.COLLATION -> Strings.JOURNAL_MEAL_SNACK
}

private fun mealEmoji(mealType: MealType): String = when (mealType) {
    MealType.PETIT_DEJEUNER -> "🌅"
    MealType.DEJEUNER -> "🍽️"
    MealType.DINER -> "🌙"
    MealType.COLLATION -> "🍎"
}
