package com.appfood.shared.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appfood.shared.model.MealType
import com.appfood.shared.model.NutrimentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the dashboard screen (DASHBOARD-01).
 * Displays daily nutriment tracking, meal summaries, and quick actions.
 *
 * Use cases will be injected when created by the SHARED agent.
 */
class DashboardViewModel(
    // TODO: Inject use cases when created by SHARED agent
    // private val getDailyQuotasUseCase: GetDailyQuotasUseCase,
    // private val getDailyEntriesUseCase: GetDailyEntriesUseCase,
    // private val getPoidsUseCase: GetPoidsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun loadDashboard() {
        _state.value = DashboardState.Loading
        viewModelScope.launch {
            // TODO: Call use cases when created by SHARED agent
            // val quotasResult = getDailyQuotasUseCase()
            // val entriesResult = getDailyEntriesUseCase()
            // val poidsResult = getPoidsUseCase()
            // Combine results into DashboardState.Success

            // Stub: simulate success with sample data
            _state.value = DashboardState.Success(
                date = "28 mars 2026",
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
        }
    }

    fun retry() {
        loadDashboard()
    }

    private fun buildStubQuotas(): List<QuotaStatusUiModel> = listOf(
        // Macros
        QuotaStatusUiModel(
            nutriment = "Proteines",
            valeurConsommee = 0.0,
            valeurCible = 82.0,
            pourcentage = 0.0,
            unite = "g",
            categorie = NutrimentCategorie.MACRO,
        ),
        QuotaStatusUiModel(
            nutriment = "Glucides",
            valeurConsommee = 0.0,
            valeurCible = 345.0,
            pourcentage = 0.0,
            unite = "g",
            categorie = NutrimentCategorie.MACRO,
        ),
        QuotaStatusUiModel(
            nutriment = "Lipides",
            valeurConsommee = 0.0,
            valeurCible = 92.0,
            pourcentage = 0.0,
            unite = "g",
            categorie = NutrimentCategorie.MACRO,
        ),
        QuotaStatusUiModel(
            nutriment = "Fibres",
            valeurConsommee = 0.0,
            valeurCible = 30.0,
            pourcentage = 0.0,
            unite = "g",
            categorie = NutrimentCategorie.MACRO,
        ),
        // Vitamines
        QuotaStatusUiModel(
            nutriment = "Vitamine B12",
            valeurConsommee = 0.0,
            valeurCible = 2.4,
            pourcentage = 0.0,
            unite = "ug",
            categorie = NutrimentCategorie.VITAMINE,
        ),
        QuotaStatusUiModel(
            nutriment = "Vitamine D",
            valeurConsommee = 0.0,
            valeurCible = 15.0,
            pourcentage = 0.0,
            unite = "ug",
            categorie = NutrimentCategorie.VITAMINE,
        ),
        QuotaStatusUiModel(
            nutriment = "Vitamine C",
            valeurConsommee = 0.0,
            valeurCible = 110.0,
            pourcentage = 0.0,
            unite = "mg",
            categorie = NutrimentCategorie.VITAMINE,
        ),
        // Mineraux
        QuotaStatusUiModel(
            nutriment = "Fer",
            valeurConsommee = 0.0,
            valeurCible = 11.0,
            pourcentage = 0.0,
            unite = "mg",
            categorie = NutrimentCategorie.MINERAL,
        ),
        QuotaStatusUiModel(
            nutriment = "Calcium",
            valeurConsommee = 0.0,
            valeurCible = 900.0,
            pourcentage = 0.0,
            unite = "mg",
            categorie = NutrimentCategorie.MINERAL,
        ),
        QuotaStatusUiModel(
            nutriment = "Zinc",
            valeurConsommee = 0.0,
            valeurCible = 12.0,
            pourcentage = 0.0,
            unite = "mg",
            categorie = NutrimentCategorie.MINERAL,
        ),
        QuotaStatusUiModel(
            nutriment = "Magnesium",
            valeurConsommee = 0.0,
            valeurCible = 420.0,
            pourcentage = 0.0,
            unite = "mg",
            categorie = NutrimentCategorie.MINERAL,
        ),
        // Acides gras
        QuotaStatusUiModel(
            nutriment = "Omega-3",
            valeurConsommee = 0.0,
            valeurCible = 2.5,
            pourcentage = 0.0,
            unite = "g",
            categorie = NutrimentCategorie.ACIDE_GRAS,
        ),
        QuotaStatusUiModel(
            nutriment = "Omega-6",
            valeurConsommee = 0.0,
            valeurCible = 10.0,
            pourcentage = 0.0,
            unite = "g",
            categorie = NutrimentCategorie.ACIDE_GRAS,
        ),
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
