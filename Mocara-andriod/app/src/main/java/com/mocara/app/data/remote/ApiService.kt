package com.mocara.app.data.remote

import com.mocara.app.data.auth.AuthInterceptor
import com.mocara.app.data.auth.TokenManager
import com.mocara.app.data.auth.TokenAuthenticator
import com.mocara.app.domain.model.ChatMessage
import com.mocara.app.domain.model.PatientSession
import com.mocara.app.domain.model.Protocol
import okhttp3.OkHttpClient
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Response

interface ApiService {
    @GET("api/v1/protocols/{drugId}")
    suspend fun getProtocol(@Path("drugId") drugId: String): Protocol

    @POST("api/v1/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): PatientSession

    @PUT("api/v1/sessions/{sessionId}/steps/{stepNumber}")
    suspend fun updateSession(
        @Path("sessionId") sessionId: String,
        @Path("stepNumber") stepNumber: Int,
        @Body request: UpdateStepRequest
    ): PatientSession

    @POST("api/v1/escalations/check")
    suspend fun checkEscalation(@Body request: EscalationCheckRequest): EscalationCheckResponse

    @POST("api/v1/chat/messages")
    suspend fun sendChatMessage(@Body request: ChatSendRequest): ChatMessage
}

interface AuthApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthTokenResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthTokenResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): AuthTokenResponse

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // 模拟器专用 IP

    private lateinit var authApiInternal: AuthApi
    private lateinit var apiServiceInternal: ApiService

    val authApi: AuthApi
        get() = authApiInternal

    val apiService: ApiService
        get() = apiServiceInternal

    fun init(tokenManager: TokenManager) {
        val authRetrofit = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        authApiInternal = authRetrofit.create(AuthApi::class.java)

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(authApiInternal, tokenManager))
            .build()

        apiServiceInternal = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

data class CreateSessionRequest(
    val drugId: String,
    val protocolId: String
)

data class UpdateStepRequest(
    val response: String
)

data class EscalationCheckRequest(
    val input: String,
    val context: List<ChatMessage> = emptyList()
)

data class EscalationCheckResponse(
    val shouldEscalate: Boolean
)

data class ChatSendRequest(
    val sessionId: String,
    val input: String,
    val context: List<ChatMessage> = emptyList()
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class AuthTokenResponse(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpiresAtMs: Long,
    val refreshToken: String,
    val refreshTokenExpiresAtMs: Long,
    val userId: Long,
    val email: String,
    val roles: Set<String>
)