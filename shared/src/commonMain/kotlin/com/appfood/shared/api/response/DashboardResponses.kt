package com.appfood.shared.api.response

import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val date: String,
    val quotasStatus: List<QuotaStatusResponse>,
    val journalDuJour: List<JournalEntryResponse>,
    val hydratation: HydratationResponse?,
    val recommandationsAliments: List<RecommandationAlimentResponse>,
    val recommandationsRecettes: List<RecommandationRecetteResponse>,
    val poidsCourant: Double?,
)

@Serializable
data class WeeklyDashboardResponse(
    val dateFrom: String,
    val dateTo: String,
    val nutritionHebdo: WeeklySummaryResponse,
    val hydratationHebdo: HydratationWeeklyResponse,
    val nutrimentsCritiques: List<String>,
    val ameliorations: List<String>,
    val degradations: List<String>,
)
