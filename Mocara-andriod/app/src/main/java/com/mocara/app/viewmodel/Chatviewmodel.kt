package com.mocara.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocara.app.data.mock.MockChatResponses
import com.mocara.app.data.repository.MockOnboardingRepository
import com.mocara.app.data.repository.RemoteOnboardingRepository
import com.mocara.app.domain.model.*
import com.mocara.app.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Chat UI State
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val session: PatientSession? = null,
    val shouldEscalate: Boolean = false,
    val escalation: Escalation? = null,
    val drugName: String = ""
)

/**
 * ChatViewModel
 * Manages controlled conversation flow with AI avatar
 */
class ChatViewModel(
//    private val repository: OnboardingRepository = MockOnboardingRepository(),
    private val repository: OnboardingRepository = RemoteOnboardingRepository()

    ) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Initialize chat with drug ID
     */
    fun initialize(drugId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load protocol for drug name
                val protocol = repository.getProtocol(drugId)
                val drugName = protocol?.drugName ?: drugId

                // Create session
                val session = repository.createSession(drugId, protocol?.id ?: "chat")

                // Send greeting
                val greeting = MockChatResponses.getGreetingMessage(drugName)

                _uiState.value = ChatUiState(
                    messages = listOf(greeting),
                    session = session,
                    drugName = drugName,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to initialize chat: ${e.message}"
                )
            }
        }
    }

    /**
     * Send user message
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val currentState = _uiState.value
            val session = currentState.session ?: return@launch

            // Add user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = UserRole.PATIENT,
                content = content
            )

            _uiState.value = currentState.copy(
                messages = currentState.messages + userMessage,
                isTyping = true
            )

            try {
                // Check for escalation first
                val shouldEscalate = repository.shouldEscalate(
                    content,
                    currentState.messages
                )

                if (shouldEscalate) {
                    handleEscalation(content)
                    return@launch
                }

                // Get AI response 模拟AI后端回复？
                val aiResponse = repository.sendMessage(
                    sessionId = session.sessionId,
                    input = content,
                    context = currentState.messages
                )

                // Check if AI response triggered escalation
                if (aiResponse.isEscalated) {
                    handleEscalation(content, aiResponse)
                } else {
                    // Add AI response
                    _uiState.value = currentState.copy(
                        messages = currentState.messages + userMessage + aiResponse,
                        isTyping = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isTyping = false,
                    error = "Failed to send message: ${e.message}"
                )
            }
        }
    }

    /**
     * Handle escalation scenario
     */
    private fun handleEscalation(userInput: String, aiResponse: ChatMessage? = null) {
        val currentState = _uiState.value

        val escalation = Escalation(
            reason = userInput,
            level = aiResponse?.let {
                when {
                    it.content.contains("911") || it.content.contains("emergency") -> EscalationLevel.CRITICAL
                    it.content.contains("immediate") -> EscalationLevel.HIGH
                    else -> EscalationLevel.MEDIUM
                }
            } ?: EscalationLevel.MEDIUM,
            contactRequired = true,
            urgency = "Immediate medical attention recommended",
            instructions = "Please follow the guidance provided and contact appropriate medical services."
        )

        val messages = if (aiResponse != null) {
            currentState.messages + aiResponse
        } else {
            currentState.messages
        }

        _uiState.value = currentState.copy(
            messages = messages,
            isTyping = false,
            shouldEscalate = true,//一旦变 true 就会执行 NavController 导航代码
            escalation = escalation
        )
    }

    /**
     * Add system message
     */
    fun addSystemMessage(content: String) {
        val currentState = _uiState.value

        val systemMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = UserRole.SYSTEM,
            content = content
        )

        _uiState.value = currentState.copy(
            messages = currentState.messages + systemMessage
        )
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reset chat
     */
    fun reset() {
        _uiState.value = ChatUiState()
    }

    /**
     * Get message count
     */
    fun getMessageCount(): Int {
        return _uiState.value.messages.size
    }

    /**
     * Get last message
     */
    fun getLastMessage(): ChatMessage? {
        return _uiState.value.messages.lastOrNull()
    }
}