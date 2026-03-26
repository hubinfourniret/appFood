package com.appfood.backend.routes

import com.appfood.backend.search.AlimentIndexer
import com.appfood.backend.search.MeilisearchClient
import com.appfood.backend.search.SearchQuery
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Routing.searchRoutes() {
    val meilisearchClient by inject<MeilisearchClient>()

    authenticate("auth-jwt") {
        route("/api/v1/aliments") {
            get("/search") {
                val q = call.request.queryParameters["q"] ?: ""
                val regime = call.request.queryParameters["regime"]
                val categorie = call.request.queryParameters["categorie"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                val filters = buildList {
                    if (regime != null) add("regime = $regime")
                    if (categorie != null) add("categorie = $categorie")
                }

                // TODO: Extract search logic into a dedicated SearchService (Routes -> Service -> DAO pattern)
                val query = SearchQuery(
                    q = q,
                    filter = filters.ifEmpty { null },
                    sort = null,
                    limit = size,
                    offset = (page - 1) * size,
                )
                val result = meilisearchClient.search(AlimentIndexer.INDEX_NAME, query)
                call.respond(result)
            }
        }
    }
}
