package com.appfood.shared.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.response.WeeklyDashboardResponse
import com.appfood.shared.data.repository.DashboardRepository
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * ViewModel for weekly dashboard screen (DASHBOARD-02).
 * Displays weekly nutriment averages, critical nutrients,
 * improvements and degradations.
 */
class WeeklyDashboardViewModel(
    private val dashboardRepository: DashboardRepository? = null,
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
            val repo = dashboardRepository
            if (repo == null) {
                _state.value = buildStubSuccess()
                return@launch
            }
            val tz = TimeZone.currentSystemDefault()
            val today = kotlinx.datetime.Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()).toLocalDateTime(tz).date
            // weekOf = date du lundi de la semaine souhaitee
            val weekOf = today.plus(currentWeekOffset * 7, DateTimeUnit.DAY)
            when (val result = repo.getWeeklyDashboard(weekOf.toString())) {
                is AppResult.Success -> {
                    _state.value = mapWeeklyResponse(result.data)
                }
                is AppResult.Error -> {
                    _state.value = WeeklyState.Error(result.message)
                }
            }
        }
    }

    private fun mapWeeklyResponse(response: WeeklyDashboardResponse): WeeklyState.Success {
        val moyennes = response.nutritionHebdo.moyenneJournaliere
        // Construire les NutrimentAverage a partir de la moyenne journaliere
        val averages = listOfNotNull(
            NutrimentAverage("Proteines", moyennes.proteines, 0.0, "g", NutrimentCategorie.MACRO),
            NutrimentAverage("Glucides", moyennes.glucides, 0.0, "g", NutrimentCategorie.MACRO),
            NutrimentAverage("Lipides", moyennes.lipides, 0.0, "g", NutrimentCategorie.MACRO),
            NutrimentAverage("Fibres", moyennes.fibres, 0.0, "g", NutrimentCategorie.MACRO),
            NutrimentAverage("Vitamine B12", moyennes.vitamineB12, 0.0, "ug", NutrimentCategorie.VITAMINE),
            NutrimentAverage("Vitamine D", moyennes.vitamineD, 0.0, "ug", NutrimentCategorie.VITAMINE),
            NutrimentAverage("Vitamine C", moyennes.vitamineC, 0.0, "mg", NutrimentCategorie.VITAMINE),
            NutrimentAverage("Fer", moyennes.fer, 0.0, "mg", NutrimentCategorie.MINERAL),
            NutrimentAverage("Calcium", moyennes.calcium, 0.0, "mg", NutrimentCategorie.MINERAL),
            NutrimentAverage("Zinc", moyennes.zinc, 0.0, "mg", NutrimentCategorie.MINERAL),
            NutrimentAverage("Magnesium", moyennes.magnesium, 0.0, "mg", NutrimentCategorie.MINERAL),
            NutrimentAverage("Omega-3", moyennes.omega3, 0.0, "g", NutrimentCategorie.ACIDE_GRAS),
            NutrimentAverage("Omega-6", moyennes.omega6, 0.0, "g", NutrimentCategorie.ACIDE_GRAS),
        )

        // Hydratation par jour
        val hydratationDaily = response.hydratationHebdo.parJour.values.map { it.quantiteMl }

        return WeeklyState.Success(
            dateFrom = response.dateFrom,
            dateTo = response.dateTo,
            canGoNext = currentWeekOffset < 0,
            nutrimentAverages = averages,
            hydratationWeekly = hydratationDaily,
            hydratationObjectifMl = response.hydratationHebdo.objectifMl,
            criticalNutrients = response.nutrimentsCritiques.map {
                NutrimentTrend(it, 0.0, 0.0, "")
            },
            improvements = response.ameliorations.map {
                NutrimentTrend(it, 0.0, 0.0, "")
            },
            degradations = response.degradations.map {
                NutrimentTrend(it, 0.0, 0.0, "")
            },
        )
    }

    private fun buildStubSuccess(): WeeklyState.Success = WeeklyState.Success(
        dateFrom = "24 mars 2026",
        dateTo = "30 mars 2026",
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
