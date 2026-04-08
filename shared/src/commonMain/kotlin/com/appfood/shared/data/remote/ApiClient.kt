package com.appfood.shared.data.remote

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
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.concurrent.Volatile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * API client wrapper around Ktor HttpClient.
 * Handles base URL, auth token, and common request configuration.
 * Intercepte les reponses 401 pour signaler l'expiration de session (AUTH-02).
 */
class ApiClient(
    val httpClient: HttpClient,
    private val baseUrl: String,
) {

    @Volatile
    private var authToken: String? = null

    /**
     * AUTH-02 : Flow emis quand le serveur retourne 401 Unauthorized.
     * L'UI observe ce flow pour rediriger vers l'ecran de login.
     */
    private val _sessionExpired = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val sessionExpired: Flow<Boolean> = _sessionExpired.asSharedFlow()

    fun setAuthToken(token: String?) {
        authToken = token
    }

    /**
     * Intercepte une reponse HTTP : si 401, clear le token et notifie l'UI.
     */
    private fun interceptResponse(response: HttpResponse): HttpResponse {
        if (response.status == HttpStatusCode.Unauthorized) {
            setAuthToken(null)
            _sessionExpired.tryEmit(true)
        }
        return response
    }

    fun buildUrl(path: String): String = "$baseUrl$path"

    suspend fun getRequest(path: String): HttpResponse {
        return interceptResponse(
            httpClient.get(buildUrl(path)) {
                authToken?.let { header("Authorization", "Bearer $it") }
            }
        )
    }

    suspend fun postRequest(path: String, body: Any): HttpResponse {
        return interceptResponse(
            httpClient.post(buildUrl(path)) {
                authToken?.let { header("Authorization", "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        )
    }

    suspend fun putRequest(path: String, body: Any): HttpResponse {
        return interceptResponse(
            httpClient.put(buildUrl(path)) {
                authToken?.let { header("Authorization", "Bearer $it") }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        )
    }

    suspend fun deleteRequest(path: String): HttpResponse {
        return interceptResponse(
            httpClient.delete(buildUrl(path)) {
                authToken?.let { header("Authorization", "Bearer $it") }
            }
        )
    }
}
