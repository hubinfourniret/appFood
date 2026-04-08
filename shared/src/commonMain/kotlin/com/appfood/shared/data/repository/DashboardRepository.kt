package com.appfood.shared.data.repository

import com.appfood.shared.api.response.DashboardResponse
import com.appfood.shared.api.response.WeeklyDashboardResponse
import com.appfood.shared.util.AppResult

/**
 * Repository interface for dashboard-related operations.
 * The dashboard aggregates multiple data sources (quotas, journal, hydratation, recommandations).
 * The backend provides a single endpoint that returns all combined data.
 */
interface DashboardRepository {

    suspend fun getDailyDashboard(date: String): AppResult<DashboardResponse>

    suspend fun getWeeklyDashboard(weekOf: String): AppResult<WeeklyDashboardResponse>
}
