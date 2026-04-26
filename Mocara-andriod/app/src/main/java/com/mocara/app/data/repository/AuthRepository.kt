package com.mocara.app.data.repository

import com.mocara.app.data.auth.AuthTokens
import com.mocara.app.data.auth.TokenManager
import com.mocara.app.data.remote.LoginRequest
import com.mocara.app.data.remote.LogoutRequest
import com.mocara.app.data.remote.RegisterRequest
import com.mocara.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String) {
        val response = RetrofitClient.authApi.login(LoginRequest(email, password))
        tokenManager.saveTokens(AuthTokens(response.accessToken, response.refreshToken))
    }

    suspend fun register(email: String, password: String) {
        val response = RetrofitClient.authApi.register(RegisterRequest(email, password))
        tokenManager.saveTokens(AuthTokens(response.accessToken, response.refreshToken))
    }

    fun isLoggedInFlow(): Flow<Boolean> = tokenManager.accessTokenFlow.map { !it.isNullOrBlank() }

    suspend fun hasToken(): Boolean = !tokenManager.getAccessToken().isNullOrBlank()

    suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        try {
            if (!refreshToken.isNullOrBlank()) {
                runCatching {
                    RetrofitClient.authApi.logout(LogoutRequest(refreshToken))
                }
            }
        } finally {
            tokenManager.clearTokens()
        }
    }
}
