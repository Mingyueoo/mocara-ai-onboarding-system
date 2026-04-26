package com.mocara.app.data.auth

import com.mocara.app.data.remote.AuthApi
import com.mocara.app.data.remote.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val path = response.request.url.encodedPath
        if (path.contains("/api/v1/auth/refresh") || path.contains("/api/v1/auth/login")) {
            return null
        }

        val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null
        val refreshed = runCatching {
            runBlocking {
                authApi.refresh(RefreshTokenRequest(refreshToken))
            }
        }.getOrNull() ?: run {
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        runBlocking {
            tokenManager.saveTokens(AuthTokens(refreshed.accessToken, refreshed.refreshToken))
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshed.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
