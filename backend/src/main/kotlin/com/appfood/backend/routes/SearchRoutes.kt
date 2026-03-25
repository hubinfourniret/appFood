package com.appfood.backend.routes

import com.appfood.backend.search.AlimentIndexer
import com.appfood.backend.search.MeilisearchClient
import com.appfood.backend.search.SearchQuery
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class SearchRequest(
    val q: String,
    val filter: List<String>? = null,
    val sort: List<String>? = null,
    val limit: Int = 20,
    val offset: Int = 0,
)

fun Routing.searchRoutes() {
    val meilisearchClient by inject<MeilisearchClient>()

    route("/api/aliments") {
        post("/search") {
            val request = call.receive<SearchRequest>()
            val query = SearchQuery(
                q = request.q,
                filter = request.filter,
                sort = request.sort,
                limit = request.limit,
                offset = request.offset,
            )
            val result = meilisearchClient.search(AlimentIndexer.INDEX_NAME, query)
            call.respond(result)
        }
    }
}
