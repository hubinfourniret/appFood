package com.appfood.shared.data.remote

import com.appfood.shared.api.response.DashboardResponse
import com.appfood.shared.api.response.WeeklyDashboardResponse
import io.ktor.client.call.body

/**
 * Client API for dashboard endpoints (/api/v1/dashboard).
 * All endpoints require authentication.
 */
class DashboardApi(private val apiClient: ApiClient) {

    suspend fun getDaily(date: String): DashboardResponse {
        return apiClient.getRequest("/api/v1/dashboard?date=$date").body()
    }

    suspend fun getWeekly(weekOf: String): WeeklyDashboardResponse {
        return apiClient.getRequest("/api/v1/dashboard/weekly?weekOf=$weekOf").body()
    }
}
