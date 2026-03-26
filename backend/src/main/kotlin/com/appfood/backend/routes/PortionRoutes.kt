package com.appfood.backend.routes

import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.CreatePortionRequest
import com.appfood.backend.routes.dto.UpdatePortionRequest
import com.appfood.backend.service.PortionService
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
import org.koin.ktor.ext.inject

fun Route.portionRoutes() {
    val portionService by inject<PortionService>()

    authenticate("auth-jwt") {
        route("/api/v1/portions") {
            // GET /portions?alimentId={id} — list portions (authenticated to include user's custom portions)
            get {
                val userId = call.userId()
                val alimentId = call.request.queryParameters["alimentId"]
                val result = portionService.listPortions(alimentId, userId)
                // PortionListResponse already has { data, total } structure
                call.respond(HttpStatusCode.OK, result)
            }

            // POST /portions — create a custom portion
            post {
                val userId = call.userId()
                val request = call.receive<CreatePortionRequest>()
                val result = portionService.createPortion(
                    userId = userId,
                    alimentId = request.alimentId,
                    nom = request.nom,
                    quantiteGrammes = request.quantiteGrammes,
                )
                call.respond(HttpStatusCode.Created, ApiResponse(data = result))
            }

            // PUT /portions/{id} — update a custom portion
            put("/{id}") {
                val userId = call.userId()
                val id = call.parameters["id"]
                    ?: throw ValidationException("ID requis")
                val request = call.receive<UpdatePortionRequest>()
                val result = portionService.updatePortion(
                    portionId = id,
                    userId = userId,
                    nom = request.nom,
                    quantiteGrammes = request.quantiteGrammes,
                )
                call.respond(HttpStatusCode.OK, ApiResponse(data = result))
            }

            // DELETE /portions/{id} — delete a custom portion
            delete("/{id}") {
                val userId = call.userId()
                val id = call.parameters["id"]
                    ?: throw ValidationException("ID requis")
                portionService.deletePortion(portionId = id, userId = userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
