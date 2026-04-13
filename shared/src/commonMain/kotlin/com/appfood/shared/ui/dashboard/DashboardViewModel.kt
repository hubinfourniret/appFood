package com.appfood.shared.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.api.response.DashboardResponse
import com.appfood.shared.api.response.QuotaStatusResponse
import com.appfood.shared.data.repository.DashboardRepository
import com.appfood.shared.data.repository.UserRepository
import com.appfood.shared.model.MealType
import com.appfood.shared.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the dashboard screen (DASHBOARD-01).
 * Displays daily nutriment tracking, meal summaries, and quick actions.
 */
class DashboardViewModel(
    private val dashboardRepository: DashboardRepository? = null,
    private val userRepository: UserRepository? = null,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun loadDashboard() {
        _state.value = DashboardState.Loading
        viewModelScope.launch {
            // Fetch onboarding status from UserRepository
            val onboardingComplete = fetchOnboardingComplete()

            val repo = dashboardRepository
            if (repo == null) {
                // Fallback stub si pas de repository injecte
                _state.value = buildStubSuccess().copy(onboardingComplete = onboardingComplete)
                return@launch
            }
            val today = kotlinx.datetime.Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            when (val result = repo.getDailyDashboard(today)) {
                is AppResult.Success -> {
                    _state.value = mapDashboardResponse(result.data).copy(onboardingComplete = onboardingComplete)
                }
                is AppResult.Error -> {
                    _state.value = DashboardState.Error(result.message)
                }
            }
        }
    }

    /**
     * Fetches onboarding completion status from UserRepository.
     * Returns true by default if repository is unavailable or request fails.
     */
    private suspend fun fetchOnboardingComplete(): Boolean {
        val repo = userRepository ?: return true
        return when (val result = repo.getCurrentUser()) {
            is AppResult.Success -> result.data.user.onboardingComplete
            is AppResult.Error -> true
        }
    }

    fun retry() {
        loadDashboard()
    }

    /**
     * Mappe la reponse API vers le state UI.
     */
    private fun mapDashboardResponse(response: DashboardResponse): DashboardState.Success {
        val quotas = response.quotasStatus.map { it.toUiModel() }
        val caloriesQuota = quotas.find { it.nutriment.equals("Calories", ignoreCase = true) }
        val caloriesConsommees = caloriesQuota?.valeurConsommee ?: 0.0
        val caloriesCible = caloriesQuota?.valeurCible ?: 0.0

        // Agreger les calories par type de repas depuis le journal du jour
        val repas = mutableMapOf<MealType, Double>()
        for (entry in response.journalDuJour) {
            val mealType = try { MealType.valueOf(entry.mealType) } catch (_: Exception) { continue }
            repas[mealType] = (repas[mealType] ?: 0.0) + entry.nutrimentsCalcules.calories
        }
        // S'assurer que tous les types de repas sont presents
        for (mt in MealType.entries) {
            if (mt !in repas) repas[mt] = 0.0
        }

        return DashboardState.Success(
            date = response.date,
            caloriesConsommees = caloriesConsommees,
            caloriesCible = caloriesCible,
            quotasStatus = quotas.filter { !it.nutriment.equals("Calories", ignoreCase = true) },
            repas = repas,
            poidsCourant = response.poidsCourant,
            hasJournalEntries = response.journalDuJour.isNotEmpty(),
        )
    }

    private fun QuotaStatusResponse.toUiModel(): QuotaStatusUiModel {
        val categorie = when {
            nutriment in setOf("Proteines", "Glucides", "Lipides", "Fibres", "Calories") -> NutrimentCategorie.MACRO
            nutriment.startsWith("Vitamine") -> NutrimentCategorie.VITAMINE
            nutriment.startsWith("Omega") -> NutrimentCategorie.ACIDE_GRAS
            else -> NutrimentCategorie.MINERAL
        }
        return QuotaStatusUiModel(
            nutriment = nutriment,
            valeurConsommee = valeurConsommee,
            valeurCible = valeurCible,
            pourcentage = pourcentage,
            unite = unite,
            categorie = categorie,
        )
    }

    private fun buildStubSuccess(): DashboardState.Success = DashboardState.Success(
        date = kotlinx.datetime.Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds()).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
        caloriesConsommees = 0.0,
        caloriesCible = 2759.0,
        quotasStatus = buildStubQuotas(),
        repas = mapOf(
            MealType.PETIT_DEJEUNER to 0.0,
            MealType.DEJEUNER to 0.0,
            MealType.DINER to 0.0,
            MealType.COLLATION to 0.0,
        ),
        poidsCourant = null,
    )

    private fun buildStubQuotas(): List<QuotaStatusUiModel> = listOf(
        QuotaStatusUiModel("Proteines", 0.0, 82.0, 0.0, "g", NutrimentCategorie.MACRO),
        QuotaStatusUiModel("Glucides", 0.0, 345.0, 0.0, "g", NutrimentCategorie.MACRO),
        QuotaStatusUiModel("Lipides", 0.0, 92.0, 0.0, "g", NutrimentCategorie.MACRO),
        QuotaStatusUiModel("Fibres", 0.0, 30.0, 0.0, "g", NutrimentCategorie.MACRO),
        QuotaStatusUiModel("Vitamine B12", 0.0, 2.4, 0.0, "ug", NutrimentCategorie.VITAMINE),
        QuotaStatusUiModel("Vitamine D", 0.0, 15.0, 0.0, "ug", NutrimentCategorie.VITAMINE),
        QuotaStatusUiModel("Vitamine C", 0.0, 110.0, 0.0, "mg", NutrimentCategorie.VITAMINE),
        QuotaStatusUiModel("Fer", 0.0, 11.0, 0.0, "mg", NutrimentCategorie.MINERAL),
        QuotaStatusUiModel("Calcium", 0.0, 900.0, 0.0, "mg", NutrimentCategorie.MINERAL),
        QuotaStatusUiModel("Zinc", 0.0, 12.0, 0.0, "mg", NutrimentCategorie.MINERAL),
        QuotaStatusUiModel("Magnesium", 0.0, 420.0, 0.0, "mg", NutrimentCategorie.MINERAL),
        QuotaStatusUiModel("Omega-3", 0.0, 2.5, 0.0, "g", NutrimentCategorie.ACIDE_GRAS),
        QuotaStatusUiModel("Omega-6", 0.0, 10.0, 0.0, "g", NutrimentCategorie.ACIDE_GRAS),
    )
}

// --- States ---

sealed interface DashboardState {
    data object Loading : DashboardState
    data class Success(
        val date: String,
        val caloriesConsommees: Double,
        val caloriesCible: Double,
        val quotasStatus: List<QuotaStatusUiModel>,
        val repas: Map<MealType, Double>,
        val poidsCourant: Double?,
        val hasJournalEntries: Boolean = false,
        val onboardingComplete: Boolean = true,
    ) : DashboardState
    data class Error(val message: String) : DashboardState
}

data class QuotaStatusUiModel(
    val nutriment: String,
    val valeurConsommee: Double,
    val valeurCible: Double,
    val pourcentage: Double,
    val unite: String,
    val categorie: NutrimentCategorie,
)

enum class NutrimentCategorie {
    MACRO, VITAMINE, MINERAL, ACIDE_GRAS
}
