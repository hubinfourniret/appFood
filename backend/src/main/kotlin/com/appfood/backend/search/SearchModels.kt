package com.appfood.backend.search

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class SearchQuery(
    val q: String,
    val filter: List<String>? = null,
    val sort: List<String>? = null,
    val limit: Int = 20,
    val offset: Int = 0,
) {
    fun toJsonString(): String {
        val obj =
            buildJsonObject {
                put("q", q)
                put("limit", limit)
                put("offset", offset)
                if (!filter.isNullOrEmpty()) {
                    put("filter", JsonArray(filter.map { JsonPrimitive(it) }))
                }
                if (!sort.isNullOrEmpty()) {
                    put("sort", JsonArray(sort.map { JsonPrimitive(it) }))
                }
            }
        return obj.toString()
    }
}

@Serializable
data class SearchResult(
    val hits: List<SearchHit> = emptyList(),
    val estimatedTotalHits: Int = 0,
    val processingTimeMs: Int = 0,
    val query: String = "",
)

@Serializable
data class SearchHit(
    val id: String,
    val nom: String,
    val categorie: String = "",
    val marque: String? = null,
    val source: String = "",
    val sourceId: String? = null,
    val regimesCompatibles: List<String> = emptyList(),
    val calories: Double = 0.0,
    val proteines: Double = 0.0,
    val glucides: Double = 0.0,
    val lipides: Double = 0.0,
    val fibres: Double = 0.0,
    val sel: Double = 0.0,
    val sucres: Double = 0.0,
    val fer: Double = 0.0,
    val calcium: Double = 0.0,
    val zinc: Double = 0.0,
    val magnesium: Double = 0.0,
    val vitamineB12: Double = 0.0,
    val vitamineD: Double = 0.0,
    val vitamineC: Double = 0.0,
    val omega3: Double = 0.0,
    val omega6: Double = 0.0,
)
