package com.appfood.backend.search

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory

class MeilisearchException(message: String, val statusCode: Int) : RuntimeException(message)

class MeilisearchClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val apiKey: String,
) {
    private val logger = LoggerFactory.getLogger(MeilisearchClient::class.java)

    private suspend fun HttpResponse.checkSuccess(operation: String): HttpResponse {
        if (!status.isSuccess()) {
            val body = runCatching { body<String>() }.getOrDefault("")
            logger.error("Meilisearch $operation echoue (${status.value}): $body")
            throw MeilisearchException("$operation echoue: ${status.value}", status.value)
        }
        return this
    }

    suspend fun createIndex(indexName: String, primaryKey: String): HttpResponse {
        return httpClient.post("$baseUrl/indexes") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody("""{"uid":"$indexName","primaryKey":"$primaryKey"}""")
        }.checkSuccess("createIndex($indexName)")
    }

    suspend fun updateSettings(indexName: String, settings: JsonObject): HttpResponse {
        return httpClient.put("$baseUrl/indexes/$indexName/settings") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(settings.toString())
        }.checkSuccess("updateSettings($indexName)")
    }

    suspend fun addDocuments(indexName: String, documents: JsonArray): HttpResponse {
        return httpClient.post("$baseUrl/indexes/$indexName/documents") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(documents.toString())
        }.checkSuccess("addDocuments($indexName, ${documents.size} docs)")
    }

    suspend fun search(indexName: String, query: SearchQuery): SearchResult {
        return httpClient.post("$baseUrl/indexes/$indexName/search") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(query.toJsonString())
        }.checkSuccess("search($indexName)").body()
    }

    suspend fun deleteIndex(indexName: String): HttpResponse {
        return httpClient.delete("$baseUrl/indexes/$indexName") {
            header("Authorization", "Bearer $apiKey")
        }.checkSuccess("deleteIndex($indexName)")
    }

    suspend fun deleteAllDocuments(indexName: String): HttpResponse {
        return httpClient.delete("$baseUrl/indexes/$indexName/documents") {
            header("Authorization", "Bearer $apiKey")
        }.checkSuccess("deleteAllDocuments($indexName)")
    }

    suspend fun health(): HttpResponse {
        return httpClient.get("$baseUrl/health")
    }
}
