package com.mocara.app.data.repository

import com.mocara.app.data.mock.MockChatResponses
import com.mocara.app.data.mock.MockProtocolData
import com.mocara.app.domain.model.ChatMessage
import com.mocara.app.domain.model.PatientSession
import com.mocara.app.domain.model.Protocol
import com.mocara.app.domain.repository.OnboardingRepository
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * MockOnboardingRepository
 * Mock implementation of OnboardingRepository
 *
 * This simulates backend API and LLM responses for development
 * Will be replaced with real implementation connecting to:
 * - Backend API for protocols
 * - LLM API for chat responses
 */
class MockOnboardingRepository : OnboardingRepository {

    // Simulate network delay
    private val networkDelayMs = 500L

    // In-memory session storage (for development)
    private val sessions = mutableMapOf<String, PatientSession>()

    /**
     * Get protocol for a specific drug
     */
    override suspend fun getProtocol(drugId: String): Protocol? {
        // Simulate network delay
        delay(networkDelayMs)

        return MockProtocolData.getProtocolByDrugId(drugId)
    }

    /**
     * Send message to AI and get response
     */
    override suspend fun sendMessage(
        sessionId: String,
        input: String,
        context: List<ChatMessage>
    ): ChatMessage {
        // Simulate thinking time
        delay(800L)

        // Get session to determine drug context
        val session = sessions[sessionId]
        val drugId = session?.drugId ?: "general"

        return MockChatResponses.generateResponse(
            input = input,
            drugId = drugId,
            context = context
        )
    }

    /**
     * Create a new patient session
     */
    override suspend fun createSession(drugId: String, protocolId: String): PatientSession {
        delay(networkDelayMs)

        val session = PatientSession(
            sessionId = UUID.randomUUID().toString(),
            drugId = drugId,
            protocolId = protocolId,
            currentStep = 0,
            isCompleted = false,
            isEscalated = false,
            startTime = System.currentTimeMillis()
        )

        // Store in memory
        sessions[session.sessionId] = session

        return session
    }

    /**
     * Update session with step completion
     */
    override suspend fun updateSession(
        session: PatientSession,
        stepNumber: Int,
        response: String
    ): PatientSession {
        delay(networkDelayMs)

        // Get protocol to check if this is the last step
        val protocol = getProtocol(session.drugId)
        val isLastStep = protocol?.steps?.size == stepNumber

        val updatedSession = session.copy(
            currentStep = stepNumber,
            isCompleted = isLastStep,
            responses = session.responses + (stepNumber to response)
        )

        // Update in memory
        sessions[session.sessionId] = updatedSession

        return updatedSession
    }

    /**
     * Check if escalation is needed
     */
    override suspend fun shouldEscalate(
        input: String,
        context: List<ChatMessage>
    ): Boolean {
        delay(200L) // Quick check

        return MockChatResponses.shouldEscalate(input, context)
    }

    /**
     * Get session by ID (utility method for development)
     */
    fun getSession(sessionId: String): PatientSession? {
        return sessions[sessionId]
    }

    /**
     * Clear all sessions (utility method for testing)
     */
    fun clearSessions() {
        sessions.clear()
    }
}