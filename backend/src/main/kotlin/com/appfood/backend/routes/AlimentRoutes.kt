package com.appfood.backend.routes

import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.service.AlimentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.alimentRoutes() {
    val alimentService by inject<AlimentService>()

    authenticate("auth-jwt") {
        route("/api/v1/aliments") {
            // Specific routes BEFORE parameterized routes (Ktor routing order matters)

            get("/search") {
                val q =
                    call.request.queryParameters["q"]
                        ?: throw ValidationException("Le parametre 'q' est requis")
                val regime = call.request.queryParameters["regime"]
                val categorie = call.request.queryParameters["categorie"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = (call.request.queryParameters["size"]?.toIntOrNull() ?: 20).coerceIn(1, 100)

                val result =
                    alimentService.search(
                        query = q,
                        regime = regime,
                        categorie = categorie,
                        page = page,
                        size = size,
                    )
                // SearchAlimentResponse already has { data, total, query } structure
                call.respond(HttpStatusCode.OK, result)
            }

            get("/barcode/{code}") {
                val code =
                    call.parameters["code"]
                        ?: throw ValidationException("Code-barres requis")
                val result = alimentService.findByBarcode(code)
                call.respond(HttpStatusCode.OK, result)
            }

            // Parameterized route LAST
            get("/{id}") {
                val id =
                    call.parameters["id"]
                        ?: throw ValidationException("ID requis")
                val result = alimentService.findById(id)
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
