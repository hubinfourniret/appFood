package com.appfood.shared.data.remote

import com.appfood.shared.api.request.LoginRequest
import com.appfood.shared.api.request.RegisterRequest
import com.appfood.shared.api.response.AuthResponse
import io.ktor.client.call.body

/**
 * Client API for auth endpoints (/api/v1/auth).
 * These endpoints do NOT require authentication (except delete).
 */
class AuthApi(private val apiClient: ApiClient) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        return apiClient.postRequest("/api/v1/auth/register", request).body()
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return apiClient.postRequest("/api/v1/auth/login", request).body()
    }

    suspend fun deleteAccount() {
        apiClient.deleteRequest("/api/v1/auth/account")
    }
}
