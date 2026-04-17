package com.appfood.backend.routes

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.search.MeilisearchClient
import com.appfood.backend.search.SearchQuery
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
)

@Serializable
data class DiagnosticResponse(
    val dbCount: Long,
    val dbSample: List<DiagnosticAliment>,
    val meiliSample: List<DiagnosticAliment>,
)

@Serializable
data class DiagnosticAliment(
    val id: String,
    val nom: String,
    val calories: Double,
    val proteines: Double,
    val glucides: Double,
)

fun Routing.healthRoutes() {
    val alimentDao by inject<AlimentDao>()
    val meili by inject<MeilisearchClient>()

    route("/api") {
        get("/health") {
            call.respond(HealthResponse(status = "ok", version = "1.0.0"))
        }

        get("/debug/aliments") {
            val q = call.request.queryParameters["q"] ?: "banane"

            val dbCount = alimentDao.count()
            val dbResults = alimentDao.findByNameLike(q, limit = 5)
            val meiliResults = try {
                meili.search("aliments", SearchQuery(q = q, limit = 5))
            } catch (e: Exception) {
                null
            }

            call.respond(
                DiagnosticResponse(
                    dbCount = dbCount,
                    dbSample = dbResults.map {
                        DiagnosticAliment(it.id, it.nom, it.calories, it.proteines, it.glucides)
                    },
                    meiliSample = meiliResults?.hits?.map {
                        DiagnosticAliment(it.id, it.nom, it.calories, it.proteines, it.glucides)
                    } ?: emptyList(),
                ),
            )
        }
    }
}
