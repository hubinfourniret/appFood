package com.appfood.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API client wrapper around Ktor HttpClient.
 * Handles base URL, auth token, and common request configuration.
 */
class ApiClient(
    val httpClient: HttpClient,
    private val baseUrl: String,
) {

    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun buildUrl(path: String): String = "$baseUrl$path"

    suspend fun getRequest(path: String): io.ktor.client.statement.HttpResponse {
        return httpClient.get(buildUrl(path)) {
            authToken?.let { header("Authorization", "Bearer $it") }
        }
    }

    suspend fun postRequest(path: String, body: Any): io.ktor.client.statement.HttpResponse {
        return httpClient.post(buildUrl(path)) {
            authToken?.let { header("Authorization", "Bearer $it") }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun putRequest(path: String, body: Any): io.ktor.client.statement.HttpResponse {
        return httpClient.put(buildUrl(path)) {
            authToken?.let { header("Authorization", "Bearer $it") }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun deleteRequest(path: String): io.ktor.client.statement.HttpResponse {
        return httpClient.delete(buildUrl(path)) {
            authToken?.let { header("Authorization", "Bearer $it") }
        }
    }
}
