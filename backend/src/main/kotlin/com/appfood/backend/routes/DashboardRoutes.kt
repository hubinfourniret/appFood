package com.appfood.backend.routes

import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.DashboardResponse
import com.appfood.backend.routes.dto.HydratationDashboardResponse
import com.appfood.backend.routes.dto.WeeklyDashboardResponse
import com.appfood.backend.service.DashboardService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import org.koin.ktor.ext.inject

fun Route.dashboardRoutes() {
    val dashboardService by inject<DashboardService>()

    authenticate("auth-jwt") {
        route("/api/v1/dashboard") {
            // GET /api/v1/dashboard/weekly?weekOf={}
            get("/weekly") {
                val userId = call.userId()
                val weekOfStr = call.request.queryParameters["weekOf"]
                val weekOf =
                    if (weekOfStr != null) {
                        parseDashboardDate(weekOfStr)
                    } else {
                        todayDashboardDate()
                    }

                val data = dashboardService.getWeeklyDashboard(userId, weekOf)

                call.respond(
                    HttpStatusCode.OK,
                    WeeklyDashboardResponse(
                        dateFrom = data.dateFrom.toString(),
                        dateTo = data.dateTo.toString(),
                        nutritionHebdo = data.nutritionHebdo,
                        hydratationHebdo = data.hydratationHebdo,
                        nutrimentsCritiques = data.nutrimentsCritiques,
                        ameliorations = data.ameliorations,
                        degradations = data.degradations,
                    ),
                )
            }

            get {
                val userId = call.userId()
                val dateStr = call.request.queryParameters["date"]
                val date =
                    if (dateStr != null) {
                        parseDashboardDate(dateStr)
                    } else {
                        todayDashboardDate()
                    }

                val data = dashboardService.getDashboard(userId, date)

                val hydratationResponse =
                    if (data.hydratationQuantiteMl != null && data.hydratationObjectifMl != null) {
                        val pourcentage =
                            if (data.hydratationObjectifMl > 0) {
                                data.hydratationQuantiteMl.toDouble() / data.hydratationObjectifMl * 100.0
                            } else {
                                0.0
                            }
                        HydratationDashboardResponse(
                            quantiteMl = data.hydratationQuantiteMl,
                            objectifMl = data.hydratationObjectifMl,
                            pourcentage = pourcentage,
                        )
                    } else {
                        null
                    }

                call.respond(
                    HttpStatusCode.OK,
                    DashboardResponse(
                        date = date.toString(),
                        quotasStatus = data.quotasStatus.map { it.toResponse() },
                        journalDuJour = data.journalEntries.map { it.toResponse() },
                        hydratation = hydratationResponse,
                        recommandationsAliments = data.recommandationsAliments.recommandations.map { it.toResponse() },
                        recommandationsRecettes = data.recommandationsRecettes.recommandations.map { it.toResponse() },
                        poidsCourant = data.poidsCourant,
                    ),
                )
            }
        }
    }
}

private fun parseDashboardDate(dateStr: String): LocalDate {
    return try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        throw com.appfood.backend.plugins.ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
    }
}

private fun todayDashboardDate(): LocalDate {
    val todayStr = kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
    return LocalDate.parse(todayStr)
}
