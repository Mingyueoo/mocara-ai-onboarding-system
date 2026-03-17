package com.mocara.app.data.remote

import com.mocara.app.domain.model.ChatMessage
import com.mocara.app.domain.model.PatientSession
import com.mocara.app.domain.model.Protocol
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // 模拟器专用 IP

    val apiService: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
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