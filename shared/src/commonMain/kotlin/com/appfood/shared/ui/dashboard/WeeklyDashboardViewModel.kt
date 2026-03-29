package com.appfood.shared.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for weekly dashboard screen (DASHBOARD-02).
 * Displays weekly nutriment averages, critical nutrients,
 * improvements and degradations.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class WeeklyDashboardViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val getWeeklySummaryUseCase: GetWeeklySummaryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<WeeklyState>(WeeklyState.Loading)
    val state: StateFlow<WeeklyState> = _state.asStateFlow()

    private var currentWeekOffset = 0

    fun init() {
        loadWeek()
    }

    fun previousWeek() {
        currentWeekOffset--
        loadWeek()
    }

    fun nextWeek() {
        if (currentWeekOffset < 0) {
            currentWeekOffset++
            loadWeek()
        }
    }

    fun retry() {
        loadWeek()
    }

    private fun loadWeek() {
        _state.value = WeeklyState.Loading
        viewModelScope.launch {
            // TODO: Call use case when created by SHARED agent
            // val result = getWeeklySummaryUseCase(weekOffset = currentWeekOffset)

            // Stub: simulate success with sample data
            val dateFrom = "24 mars 2026"
            val dateTo = "30 mars 2026"

            _state.value = WeeklyState.Success(
                dateFrom = dateFrom,
                dateTo = dateTo,
                canGoNext = currentWeekOffset < 0,
                nutrimentAverages = buildStubAverages(),
                hydratationWeekly = listOf(1500, 1800, 2000, 1200, 2100, 1900, 0),
                hydratationObjectifMl = 2000,
                criticalNutrients = listOf(
                    NutrimentTrend("Vitamine B12", 0.8, 2.4, "ug"),
                    NutrimentTrend("Fer", 5.5, 11.0, "mg"),
                ),
                improvements = listOf(
                    NutrimentTrend("Proteines", 75.0, 82.0, "g"),
                ),
                degradations = listOf(
                    NutrimentTrend("Calcium", 450.0, 900.0, "mg"),
                ),
            )
        }
    }

    private fun buildStubAverages(): List<NutrimentAverage> = listOf(
        NutrimentAverage("Proteines", 65.0, 82.0, "g", NutrimentCategorie.MACRO),
        NutrimentAverage("Glucides", 280.0, 345.0, "g", NutrimentCategorie.MACRO),
        NutrimentAverage("Lipides", 75.0, 92.0, "g", NutrimentCategorie.MACRO),
        NutrimentAverage("Fibres", 22.0, 30.0, "g", NutrimentCategorie.MACRO),
        NutrimentAverage("Vitamine B12", 0.8, 2.4, "ug", NutrimentCategorie.VITAMINE),
        NutrimentAverage("Vitamine D", 5.0, 15.0, "ug", NutrimentCategorie.VITAMINE),
        NutrimentAverage("Vitamine C", 85.0, 110.0, "mg", NutrimentCategorie.VITAMINE),
        NutrimentAverage("Fer", 5.5, 11.0, "mg", NutrimentCategorie.MINERAL),
        NutrimentAverage("Calcium", 450.0, 900.0, "mg", NutrimentCategorie.MINERAL),
        NutrimentAverage("Zinc", 8.0, 12.0, "mg", NutrimentCategorie.MINERAL),
        NutrimentAverage("Omega-3", 1.2, 2.5, "g", NutrimentCategorie.ACIDE_GRAS),
    )
}

// --- States ---

sealed interface WeeklyState {
    data object Loading : WeeklyState
    data class Success(
        val dateFrom: String,
        val dateTo: String,
        val canGoNext: Boolean,
        val nutrimentAverages: List<NutrimentAverage>,
        val hydratationWeekly: List<Int>,
        val hydratationObjectifMl: Int,
        val criticalNutrients: List<NutrimentTrend>,
        val improvements: List<NutrimentTrend>,
        val degradations: List<NutrimentTrend>,
    ) : WeeklyState
    data class Error(val message: String) : WeeklyState
}

data class NutrimentAverage(
    val nutriment: String,
    val moyenneConsommee: Double,
    val cible: Double,
    val unite: String,
    val categorie: NutrimentCategorie,
)

data class NutrimentTrend(
    val nutriment: String,
    val valeur: Double,
    val cible: Double,
    val unite: String,
)
