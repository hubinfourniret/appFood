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
    private val dashboardRepository: DashboardRepository,
    private val userRepository: UserRepository? = null,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun loadDashboard() {
        _state.value = DashboardState.Loading
        viewModelScope.launch {
            // Fetch onboarding status from UserRepository
            val onboardingComplete = fetchOnboardingComplete()

            val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            when (val result = dashboardRepository.getDailyDashboard(today)) {
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

        // Agreger les calories par type de repas + collecter les items pour l'onglet Repas (TACHE-514)
        val repas = mutableMapOf<MealType, Double>()
        val entriesParRepas = MealType.entries.associateWith { mutableListOf<JournalEntryUiModel>() }
        for (entry in response.journalDuJour) {
            val mealType = try { MealType.valueOf(entry.mealType) } catch (_: Exception) { continue }
            repas[mealType] = (repas[mealType] ?: 0.0) + entry.nutrimentsCalcules.calories
            entriesParRepas[mealType]?.add(
                JournalEntryUiModel(
                    id = entry.id,
                    nom = entry.nom,
                    quantiteGrammes = entry.quantiteGrammes,
                    nbPortions = entry.nbPortions,
                    calories = entry.nutrimentsCalcules.calories,
                    isRecette = entry.recetteId != null,
                ),
            )
        }
        for (mt in MealType.entries) {
            if (mt !in repas) repas[mt] = 0.0
        }

        return DashboardState.Success(
            date = response.date,
            caloriesConsommees = caloriesConsommees,
            caloriesCible = caloriesCible,
            quotasStatus = quotas.filter { !it.nutriment.equals("Calories", ignoreCase = true) },
            repas = repas,
            entriesParRepas = entriesParRepas.mapValues { it.value.toList() },
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
        val entriesParRepas: Map<MealType, List<JournalEntryUiModel>> = emptyMap(),
        val poidsCourant: Double?,
        val hasJournalEntries: Boolean = false,
        val onboardingComplete: Boolean = true,
    ) : DashboardState
    data class Error(val message: String) : DashboardState
}

/** Resume d'une entree de journal pour l'onglet Repas (TACHE-514). */
data class JournalEntryUiModel(
    val id: String,
    val nom: String,
    val quantiteGrammes: Double,
    val nbPortions: Double?,
    val calories: Double,
    val isRecette: Boolean,
)

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
