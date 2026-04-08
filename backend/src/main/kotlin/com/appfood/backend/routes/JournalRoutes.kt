package com.appfood.backend.routes

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.JournalEntryRow
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.AddJournalEntryRequest
import com.appfood.backend.routes.dto.DailySummaryResponse
import com.appfood.backend.routes.dto.JournalEntryResponse
import com.appfood.backend.routes.dto.JournalListResponse
import com.appfood.backend.routes.dto.NutrimentValuesResponse
import com.appfood.backend.routes.dto.RecentAlimentResponse
import com.appfood.backend.routes.dto.UpdateJournalEntryRequest
import com.appfood.backend.routes.dto.WeeklySummaryResponse
import com.appfood.backend.service.JournalService
import com.appfood.backend.service.NutrientSums
import com.appfood.backend.service.ProfileService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import org.koin.ktor.ext.inject

fun Route.journalRoutes() {
    val journalService by inject<JournalService>()
    val alimentDao by inject<AlimentDao>()
    val profileService by inject<ProfileService>()

    authenticate("auth-jwt") {
        route("/api/v1/journal") {
            // Specific routes BEFORE parameterized routes

            // --- Favoris endpoints (JOURNAL-03) ---

            get("/favoris") {
                val userId = call.userId()
                val favorisIds = profileService.getFavorisIds(userId)
                val aliments =
                    favorisIds.mapNotNull { id ->
                        alimentDao.findById(id)
                    }
                val entries =
                    aliments.map { aliment ->
                        JournalEntryResponse(
                            id = "fav-${aliment.id}",
                            date = "",
                            mealType = "",
                            alimentId = aliment.id,
                            recetteId = null,
                            nom = aliment.nom,
                            quantiteGrammes = 0.0,
                            nbPortions = null,
                            nutrimentsCalcules =
                                NutrimentValuesResponse(
                                    calories = aliment.calories,
                                    proteines = aliment.proteines,
                                    glucides = aliment.glucides,
                                    lipides = aliment.lipides,
                                    fibres = aliment.fibres,
                                    sel = aliment.sel,
                                    sucres = aliment.sucres,
                                    fer = aliment.fer,
                                    calcium = aliment.calcium,
                                    zinc = aliment.zinc,
                                    magnesium = aliment.magnesium,
                                    vitamineB12 = aliment.vitamineB12,
                                    vitamineD = aliment.vitamineD,
                                    vitamineC = aliment.vitamineC,
                                    omega3 = aliment.omega3,
                                    omega6 = aliment.omega6,
                                ),
                            createdAt = "",
                            updatedAt = "",
                        )
                    }
                call.respond(
                    HttpStatusCode.OK,
                    JournalListResponse(data = entries, total = entries.size),
                )
            }

            post("/favoris/{alimentId}") {
                val userId = call.userId()
                val alimentId =
                    call.parameters["alimentId"]
                        ?: throw ValidationException("alimentId requis")
                // Verify aliment exists
                alimentDao.findById(alimentId)
                    ?: throw com.appfood.backend.plugins.NotFoundException("Aliment non trouve: $alimentId")
                profileService.addFavori(userId, alimentId)
                call.respond(HttpStatusCode.Created, mapOf("alimentId" to alimentId))
            }

            delete("/favoris/{alimentId}") {
                val userId = call.userId()
                val alimentId =
                    call.parameters["alimentId"]
                        ?: throw ValidationException("alimentId requis")
                profileService.removeFavori(userId, alimentId)
                call.respond(HttpStatusCode.NoContent)
            }

            get("/summary") {
                val userId = call.userId()
                val dateStr = call.request.queryParameters["date"]
                val date =
                    if (dateStr != null) {
                        parseLocalDate(dateStr)
                    } else {
                        todayDate()
                    }
                val summary = journalService.getDailySummary(userId, date)
                call.respond(
                    HttpStatusCode.OK,
                    DailySummaryResponse(
                        date = summary.date.toString(),
                        totalNutriments = summary.totalNutriments.toResponse(),
                        parRepas =
                            summary.parRepas.map { (mealType, sums) ->
                                mealType.name to sums.toResponse()
                            }.toMap(),
                        nbEntrees = summary.nbEntrees,
                    ),
                )
            }

            get("/summary/weekly") {
                val userId = call.userId()
                val weekOfStr = call.request.queryParameters["weekOf"]
                val weekOf =
                    if (weekOfStr != null) {
                        parseLocalDate(weekOfStr)
                    } else {
                        todayDate()
                    }
                val summary = journalService.getWeeklySummary(userId, weekOf)
                call.respond(
                    HttpStatusCode.OK,
                    WeeklySummaryResponse(
                        dateFrom = summary.dateFrom.toString(),
                        dateTo = summary.dateTo.toString(),
                        moyenneJournaliere = summary.moyenneJournaliere.toResponse(),
                        parJour =
                            summary.parJour.map { (date, sums) ->
                                date.toString() to sums.toResponse()
                            }.toMap(),
                        joursAvecSaisie = summary.joursAvecSaisie,
                    ),
                )
            }

            get("/recents") {
                val userId = call.userId()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val alimentIds = journalService.getRecentAlimentIds(userId, limit)

                val recents =
                    alimentIds.mapNotNull { id ->
                        alimentDao.findById(id)?.let { aliment ->
                            RecentAlimentResponse(
                                id = aliment.id,
                                nom = aliment.nom,
                                categorie = aliment.categorie,
                            )
                        }
                    }

                call.respond(HttpStatusCode.OK, recents)
            }

            // GET /journal — list entries
            get {
                val userId = call.userId()
                val dateStr = call.request.queryParameters["date"]
                val dateFromStr = call.request.queryParameters["dateFrom"]
                val dateToStr = call.request.queryParameters["dateTo"]
                val mealType = call.request.queryParameters["mealType"]

                val date = dateStr?.let { parseLocalDate(it) }
                val dateFrom = dateFromStr?.let { parseLocalDate(it) }
                val dateTo = dateToStr?.let { parseLocalDate(it) }

                val entries = journalService.getEntries(userId, date, dateFrom, dateTo, mealType)
                call.respond(
                    HttpStatusCode.OK,
                    JournalListResponse(
                        data = entries.map { it.toResponse() },
                        total = entries.size,
                    ),
                )
            }

            // POST /journal — add entry
            post {
                val userId = call.userId()
                val request = call.receive<AddJournalEntryRequest>()
                val entry =
                    journalService.addEntry(
                        userId = userId,
                        idParam = request.id,
                        dateStr = request.date,
                        mealTypeStr = request.mealType,
                        alimentId = request.alimentId,
                        recetteId = request.recetteId,
                        quantiteGrammes = request.quantiteGrammes,
                        nbPortions = request.nbPortions,
                    )
                call.respond(HttpStatusCode.Created, entry.toResponse())
            }

            // PUT /journal/{id} — update entry
            put("/{id}") {
                val userId = call.userId()
                val entryId =
                    call.parameters["id"]
                        ?: throw ValidationException("ID requis")
                val request = call.receive<UpdateJournalEntryRequest>()
                val entry =
                    journalService.updateEntry(
                        userId = userId,
                        entryId = entryId,
                        quantiteGrammes = request.quantiteGrammes,
                        nbPortions = request.nbPortions,
                        mealTypeStr = request.mealType,
                    )
                call.respond(HttpStatusCode.OK, entry.toResponse())
            }

            // DELETE /journal/{id}
            delete("/{id}") {
                val userId = call.userId()
                val entryId =
                    call.parameters["id"]
                        ?: throw ValidationException("ID requis")
                journalService.deleteEntry(userId, entryId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

// --- Mapping helpers ---

internal fun JournalEntryRow.toResponse() =
    JournalEntryResponse(
        id = id,
        date = date.toString(),
        mealType = mealType.name,
        alimentId = alimentId,
        recetteId = recetteId,
        nom = nom,
        quantiteGrammes = quantiteGrammes,
        nbPortions = nbPortions,
        nutrimentsCalcules =
            NutrimentValuesResponse(
                calories = calories,
                proteines = proteines,
                glucides = glucides,
                lipides = lipides,
                fibres = fibres,
                sel = sel,
                sucres = sucres,
                fer = fer,
                calcium = calcium,
                zinc = zinc,
                magnesium = magnesium,
                vitamineB12 = vitamineB12,
                vitamineD = vitamineD,
                vitamineC = vitamineC,
                omega3 = omega3,
                omega6 = omega6,
            ),
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

internal fun NutrientSums.toResponse() =
    NutrimentValuesResponse(
        calories = calories,
        proteines = proteines,
        glucides = glucides,
        lipides = lipides,
        fibres = fibres,
        sel = sel,
        sucres = sucres,
        fer = fer,
        calcium = calcium,
        zinc = zinc,
        magnesium = magnesium,
        vitamineB12 = vitamineB12,
        vitamineD = vitamineD,
        vitamineC = vitamineC,
        omega3 = omega3,
        omega6 = omega6,
    )

private fun parseLocalDate(dateStr: String): LocalDate {
    return try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        throw ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
    }
}

private fun todayDate(): LocalDate {
    val todayStr = kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
    return LocalDate.parse(todayStr)
}
