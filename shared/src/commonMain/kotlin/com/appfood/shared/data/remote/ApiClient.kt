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
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.concurrent.Volatile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * API client wrapper around Ktor HttpClient.
 * Handles base URL, auth token, and common request configuration.
 * Intercepte les reponses 401 pour signaler l'expiration de session (AUTH-02).
 */
class ApiClient(
    val httpClient: HttpClient,
    private val baseUrl: String,
    private val onTokenCleared: (() -> Unit)? = null,
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
     * Intercepte une reponse HTTP :
     * - 401 → clear le token et notifie l'UI
     * - Tout status non-2xx → lance une ApiException avec le message du backend
     */
    private suspend fun interceptResponse(response: HttpResponse): HttpResponse {
        if (response.status == HttpStatusCode.Unauthorized) {
            setAuthToken(null)
            onTokenCleared?.invoke()
            _sessionExpired.tryEmit(true)
        }
        if (!response.status.isSuccess()) {
            val message = try {
                val body = response.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject
                // Backend error format: {"error":{"code":"...","message":"..."}} or {"message":"..."}
                json["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                    ?: json["message"]?.jsonPrimitive?.content
                    ?: body
            } catch (_: Exception) {
                "Erreur ${response.status.value}"
            }
            throw ApiException(response.status.value, message)
        }
        return response
    }

    class ApiException(val statusCode: Int, override val message: String) : Exception(message)

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
