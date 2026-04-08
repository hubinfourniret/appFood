package com.appfood.backend.routes

import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.AddPoidsRequest
import com.appfood.backend.service.PoidsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import org.koin.ktor.ext.inject

fun Route.poidsRoutes() {
    val poidsService by inject<PoidsService>()

    authenticate("auth-jwt") {
        route("/api/v1/poids") {
            // GET /api/v1/poids?dateFrom={}&dateTo={}
            get {
                val userId = call.userId()
                val dateFromStr = call.request.queryParameters["dateFrom"]
                val dateToStr = call.request.queryParameters["dateTo"]

                val dateFrom = dateFromStr?.let { parsePoidsDate(it) }
                val dateTo = dateToStr?.let { parsePoidsDate(it) }

                val response = poidsService.getHistory(userId, dateFrom, dateTo)
                call.respond(HttpStatusCode.OK, response)
            }

            // POST /api/v1/poids
            post {
                val userId = call.userId()
                val request = call.receive<AddPoidsRequest>()
                val response = poidsService.addPoids(userId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            // DELETE /api/v1/poids/{id}
            delete("/{id}") {
                val userId = call.userId()
                val poidsId = call.parameters["id"]!!
                poidsService.deletePoids(userId, poidsId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun parsePoidsDate(dateStr: String): LocalDate {
    return try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        throw ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
    }
}
