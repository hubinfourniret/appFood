package com.appfood.shared.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.response.JournalEntryResponse
import com.appfood.shared.data.repository.JournalRepository
import com.appfood.shared.model.MealType
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * TACHE-515 : ViewModel pour la vue hebdomadaire des repas saisis.
 * Recupere les 7 derniers jours du journal et les groupe par jour + repas.
 */
class WeeklyRepasViewModel(
    private val journalRepository: JournalRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<WeeklyRepasState>(WeeklyRepasState.Loading)
    val state: StateFlow<WeeklyRepasState> = _state.asStateFlow()

    fun load() {
        _state.value = WeeklyRepasState.Loading
        viewModelScope.launch {
            val tz = TimeZone.currentSystemDefault()
            val today = kotlin.time.Clock.System.now().toLocalDateTime(tz).date
            val weekAgo = today.minus(6, DateTimeUnit.DAY)

            when (val result = journalRepository.getEntriesRange(
                dateFrom = weekAgo.toString(),
                dateTo = today.toString(),
            )) {
                is AppResult.Success -> {
                    _state.value = buildState(weekAgo, today, result.data.data)
                }
                is AppResult.Error -> {
                    _state.value = WeeklyRepasState.Error(result.message)
                }
            }
        }
    }

    private fun buildState(
        weekAgo: LocalDate,
        today: LocalDate,
        entries: List<JournalEntryResponse>,
    ): WeeklyRepasState.Success {
        // Construire la liste des 7 jours ordonnee du plus ancien au plus recent
        val days = (0..6).map { offset ->
            val date = LocalDate.fromEpochDays(weekAgo.toEpochDays() + offset)
            val entriesForDay = entries.filter { it.date == date.toString() }
            val byMeal = MealType.entries.associateWith { mealType ->
                entriesForDay.filter {
                    runCatching { MealType.valueOf(it.mealType) }.getOrNull() == mealType
                }.map { entry ->
                    JournalEntryUiModel(
                        id = entry.id,
                        nom = entry.nom,
                        quantiteGrammes = entry.quantiteGrammes,
                        nbPortions = entry.nbPortions,
                        calories = entry.nutrimentsCalcules.calories,
                        isRecette = entry.recetteId != null,
                        recetteId = entry.recetteId,
                        ingredientOverrides = entry.ingredientOverrides,
                    )
                }
            }
            val totalCalories = entriesForDay.sumOf { it.nutrimentsCalcules.calories }
            DaySummary(
                date = date,
                totalCalories = totalCalories,
                entriesByMeal = byMeal,
            )
        }
        return WeeklyRepasState.Success(
            dateFrom = weekAgo,
            dateTo = today,
            days = days,
        )
    }
}

sealed interface WeeklyRepasState {
    data object Loading : WeeklyRepasState
    data class Success(
        val dateFrom: LocalDate,
        val dateTo: LocalDate,
        val days: List<DaySummary>,
    ) : WeeklyRepasState
    data class Error(val message: String) : WeeklyRepasState
}

data class DaySummary(
    val date: LocalDate,
    val totalCalories: Double,
    val entriesByMeal: Map<MealType, List<JournalEntryUiModel>>,
)
