package com.mocara.app.data.repository

import com.mocara.app.data.remote.ApiService
import com.mocara.app.data.remote.ChatSendRequest
import com.mocara.app.data.remote.CreateSessionRequest
import com.mocara.app.data.remote.EscalationCheckRequest
import com.mocara.app.data.remote.RetrofitClient
import com.mocara.app.data.remote.UpdateStepRequest
import com.mocara.app.domain.model.ChatMessage
import com.mocara.app.domain.model.PatientSession
import com.mocara.app.domain.model.Protocol
import com.mocara.app.domain.repository.OnboardingRepository

/**
 * RemoteOnboardingRepository
 * Real implementation of OnboardingRepository backed by Spring Boot APIs.
 */
class RemoteOnboardingRepository(
    private val api: ApiService = RetrofitClient.apiService
) : OnboardingRepository {

    override suspend fun getProtocol(drugId: String): Protocol? {
        // Backend throws 400 if not found; if you later change to 404, handle it here.
        return api.getProtocol(drugId)
    }

    override suspend fun sendMessage(
        sessionId: String,
        input: String,
        context: List<ChatMessage>
    ): ChatMessage {
        return api.sendChatMessage(
            ChatSendRequest(
                sessionId = sessionId,
                input = input,
                context = context
            )
        )
    }

    override suspend fun createSession(
        drugId: String,
        protocolId: String
    ): PatientSession {
        return api.createSession(
            CreateSessionRequest(
                drugId = drugId,
                protocolId = protocolId
            )
        )
    }

    override suspend fun updateSession(
        session: PatientSession,
        stepNumber: Int,
        response: String
    ): PatientSession {
        return api.updateSession(
            sessionId = session.sessionId,
            stepNumber = stepNumber,
            request = UpdateStepRequest(response = response)
        )
    }

    override suspend fun shouldEscalate(
        input: String,
        context: List<ChatMessage>
    ): Boolean {
        // 1. 预检查：空字符串直接视为不需要升级，节省网络开销
        if (input.isBlank()) {
            return false
        }

        return try {
            // 2. 尝试请求后端
            val response = api.checkEscalation(
                EscalationCheckRequest(
                    input = input,
                    context = context
                )
            )
            response.shouldEscalate
        } catch (e: Exception) {
            // 3. 异常降级处理：
            // 无论是 400 (Validation failed), 500 (Server Error) 还是断网
            // 我们都将其视为「不需要升级」，保证用户能继续 onboarding 流程
            android.util.Log.w("RemoteRepo", "Escalation check failed, bypassing: ${e.message}")
            false
        }
    }
}