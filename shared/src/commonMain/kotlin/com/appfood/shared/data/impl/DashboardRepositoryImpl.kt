package com.appfood.shared.data.impl

import com.appfood.shared.api.response.DashboardResponse
import com.appfood.shared.api.response.WeeklyDashboardResponse
import com.appfood.shared.data.remote.DashboardApi
import com.appfood.shared.data.repository.DashboardRepository
import com.appfood.shared.util.AppResult

/**
 * Fetches dashboard data from the remote API.
 * The backend aggregates quotas, journal, hydratation and recommandations
 * into a single response.
 */
class DashboardRepositoryImpl(
    private val dashboardApi: DashboardApi,
) : DashboardRepository {

    override suspend fun getDailyDashboard(date: String): AppResult<DashboardResponse> {
        return try {
            val response = dashboardApi.getDaily(date)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch daily dashboard",
                cause = e,
            )
        }
    }

    override suspend fun getWeeklyDashboard(weekOf: String): AppResult<WeeklyDashboardResponse> {
        return try {
            val response = dashboardApi.getWeekly(weekOf)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(
                code = "NETWORK_ERROR",
                message = e.message ?: "Failed to fetch weekly dashboard",
                cause = e,
            )
        }
    }
}
