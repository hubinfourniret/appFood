package com.appfood.backend.routes

import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.AddHydratationRequest
import com.appfood.backend.routes.dto.UpdateHydratationObjectifRequest
import com.appfood.backend.service.HydratationService
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

fun Route.hydratationRoutes() {
    val hydratationService by inject<HydratationService>()

    authenticate("auth-jwt") {
        route("/api/v1/hydratation") {
            // GET /api/v1/hydratation?date={}
            get {
                val userId = call.userId()
                val dateStr = call.request.queryParameters["date"]
                val date =
                    if (dateStr != null) {
                        parseHydratationDate(dateStr)
                    } else {
                        todayHydratationDate()
                    }
                val response = hydratationService.getDaily(userId, date)
                call.respond(HttpStatusCode.OK, response)
            }

            // GET /api/v1/hydratation/weekly?weekOf={}
            get("/weekly") {
                val userId = call.userId()
                val weekOfStr = call.request.queryParameters["weekOf"]
                val weekOf =
                    if (weekOfStr != null) {
                        parseHydratationDate(weekOfStr)
                    } else {
                        todayHydratationDate()
                    }
                val response = hydratationService.getWeekly(userId, weekOf)
                call.respond(HttpStatusCode.OK, response)
            }

            // POST /api/v1/hydratation — ajouter une entree
            post {
                val userId = call.userId()
                val request = call.receive<AddHydratationRequest>()
                val response = hydratationService.addEntry(userId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            // PUT /api/v1/hydratation/objectif — personnaliser l'objectif
            put("/objectif") {
                val userId = call.userId()
                val request = call.receive<UpdateHydratationObjectifRequest>()
                val response = hydratationService.updateObjectif(userId, request)
                call.respond(HttpStatusCode.OK, response)
            }

            // POST /api/v1/hydratation/objectif/reset — reinitialiser l'objectif auto
            post("/objectif/reset") {
                val userId = call.userId()
                val response = hydratationService.resetObjectif(userId)
                call.respond(HttpStatusCode.OK, response)
            }

            // DELETE /api/v1/hydratation/entries/{entryId} — supprimer une entree
            delete("/entries/{entryId}") {
                val userId = call.userId()
                val entryId = call.parameters["entryId"]
                    ?: throw ValidationException("entryId requis")
                val response = hydratationService.deleteEntry(userId, entryId)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}

private fun parseHydratationDate(dateStr: String): LocalDate {
    return try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        throw ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
    }
}

private fun todayHydratationDate(): LocalDate {
    val todayStr = kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
    return LocalDate.parse(todayStr)
}
