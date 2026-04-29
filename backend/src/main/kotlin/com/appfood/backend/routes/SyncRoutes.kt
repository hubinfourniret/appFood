package com.appfood.backend.routes

import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.JournalEntryResponse
import com.appfood.backend.routes.dto.NutrimentValuesResponse
import com.appfood.backend.routes.dto.PoidsResponse
import com.appfood.backend.routes.dto.QuotaResponse
import com.appfood.backend.routes.dto.SyncPullResponse
import com.appfood.backend.routes.dto.SyncPushRequest
import com.appfood.backend.service.JournalService
import com.appfood.backend.service.PoidsService
import com.appfood.backend.service.QuotaService
import com.appfood.backend.service.SyncService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.koin.ktor.ext.inject

fun Route.syncRoutes() {
    val syncService by inject<SyncService>()
    val journalService by inject<JournalService>()
    val poidsService by inject<PoidsService>()
    val quotaService by inject<QuotaService>()

    authenticate("auth-jwt") {
        route("/api/v1/sync") {
            // POST /api/v1/sync/push — client pushes offline entries
            post("/push") {
                val userId = call.userId()
                val request = call.receive<SyncPushRequest>()
                val result = syncService.push(userId, request)
                call.respond(HttpStatusCode.OK, result)
            }

            // GET /api/v1/sync/pull?since={ISO date or timestamp} — client pulls latest server state
            get("/pull") {
                val userId = call.userId()
                val sinceStr = call.request.queryParameters["since"]
                    ?: throw ValidationException("Le parametre 'since' est requis (format: YYYY-MM-DD ou ISO-8601)")

                val sinceDate = try {
                    LocalDate.parse(sinceStr.substringBefore("T"))
                } catch (e: Exception) {
                    throw ValidationException("Format 'since' invalide: '$sinceStr'. Attendu: YYYY-MM-DD")
                }

                val todayDate = Clock.System.now().toString().substringBefore("T").let { LocalDate.parse(it) }

                val journalEntries = try {
                    journalService.getEntries(
                        userId = userId,
                        date = null,
                        dateFrom = sinceDate,
                        dateTo = todayDate,
                        mealTypeStr = null,
                    ).map { row ->
                        JournalEntryResponse(
                            id = row.id,
                            date = row.date.toString(),
                            mealType = row.mealType.name,
                            alimentId = row.alimentId,
                            recetteId = row.recetteId,
                            nom = row.nom,
                            quantiteGrammes = row.quantiteGrammes,
                            nbPortions = row.nbPortions,
                            nutrimentsCalcules = NutrimentValuesResponse(
                                calories = row.calories,
                                proteines = row.proteines,
                                glucides = row.glucides,
                                lipides = row.lipides,
                                fibres = row.fibres,
                                sel = row.sel,
                                sucres = row.sucres,
                                fer = row.fer,
                                calcium = row.calcium,
                                zinc = row.zinc,
                                magnesium = row.magnesium,
                                vitamineB12 = row.vitamineB12,
                                vitamineD = row.vitamineD,
                                vitamineC = row.vitamineC,
                                omega3 = row.omega3,
                                omega6 = row.omega6,
                            ),
                            createdAt = row.createdAt.toString(),
                            updatedAt = row.updatedAt.toString(),
                        )
                    }
                } catch (_: Exception) {
                    emptyList()
                }

                val poidsEntries = try {
                    poidsService.getHistory(userId, sinceDate, todayDate).data
                } catch (_: Exception) {
                    emptyList<PoidsResponse>()
                }

                val quotas = try {
                    quotaService.getAllQuotas(userId).map { q ->
                        QuotaResponse(
                            nutriment = q.nutriment.name,
                            valeurCible = q.valeurCible,
                            estPersonnalise = q.estPersonnalise,
                            valeurCalculee = q.valeurCalculee,
                            unite = q.unite,
                            updatedAt = q.updatedAt.toString(),
                        )
                    }
                } catch (_: Exception) {
                    emptyList()
                }

                call.respond(
                    HttpStatusCode.OK,
                    SyncPullResponse(
                        journalEntries = journalEntries,
                        poidsEntries = poidsEntries,
                        hydratationEntries = emptyList(),
                        quotas = quotas,
                        timestamp = Clock.System.now().toString(),
                    ),
                )
            }
        }
    }
}
