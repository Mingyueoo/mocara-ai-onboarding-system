package com.mocara.app.domain.repository

import com.mocara.app.domain.model.ChatMessage
import com.mocara.app.domain.model.Protocol
import com.mocara.app.domain.model.PatientSession

/**
 * OnboardingRepository - Interface for Protocol and AI interactions
 *
 * This interface will be implemented by:
 * - MockOnboardingRepository (current - for development)
 * - RealOnboardingRepository (future - with backend API + LLM)
 */
interface OnboardingRepository {

    /**
     * Get protocol for a specific drug
     * @param drugId Drug identifier (e.g., "ozempic")
     * @return Protocol with steps and metadata
     */
    suspend fun getProtocol(drugId: String): Protocol?

    /**
     * Send message to AI and get response
     * @param sessionId Current session ID
     * @param input User input message
     * @param context Current conversation context
     * @return AI response as ChatMessage
     */
    suspend fun sendMessage(
        sessionId: String,
        input: String,
        context: List<ChatMessage> = emptyList()
    ): ChatMessage

    /**
     * Create a new patient session
     * @param drugId Drug identifier
     * @param protocolId Protocol identifier
     * @return New session object
     */
    suspend fun createSession(drugId: String, protocolId: String): PatientSession

    /**
     * Update session with step completion
     * @param session Current session
     * @param stepNumber Step that was completed
     * @param response User's response/answer
     * @return Updated session
     */
    suspend fun updateSession(
        session: PatientSession,
        stepNumber: Int,
        response: String
    ): PatientSession

    /**
     * Check if escalation is needed based on input
     * @param input User input to analyze
     * @param context Conversation context
     * @return True if escalation is required
     */
    suspend fun shouldEscalate(
        input: String,
        context: List<ChatMessage> = emptyList()
    ): Boolean
}