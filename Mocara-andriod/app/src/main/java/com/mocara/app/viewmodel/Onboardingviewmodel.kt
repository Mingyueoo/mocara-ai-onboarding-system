package com.mocara.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mocara.app.data.repository.MockOnboardingRepository
import com.mocara.app.data.repository.RemoteOnboardingRepository
import com.mocara.app.domain.model.*
import com.mocara.app.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Onboarding UI State
 */
data class OnboardingUiState(
    val protocol: Protocol? = null,
    val currentStep: ProtocolStep? = null,
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val session: PatientSession? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val shouldEscalate: Boolean = false,
    val escalation: Escalation? = null
)

/**
 * OnboardingViewModel
 * Core state machine for step-based medication onboarding
 */
class OnboardingViewModel(
//    private val repository: OnboardingRepository = MockOnboardingRepository()
    private val repository: OnboardingRepository = RemoteOnboardingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /**
     * Initialize onboarding with drug ID
     */
    fun initialize(drugId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load protocol
                val protocol = repository.getProtocol(drugId)

                if (protocol == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Protocol not found for drug: $drugId"
                    )
                    return@launch
                }

                // Create session
                val session = repository.createSession(drugId, protocol.id)

                // Set first step
                _uiState.value = OnboardingUiState(
                    protocol = protocol,
                    currentStep = protocol.steps.firstOrNull(),
                    currentStepIndex = 0,
                    totalSteps = protocol.steps.size,
                    session = session,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load protocol: ${e.message}"
                )
            }
        }
    }

    /**
     * Move to next step
     */
    fun nextStep(userResponse: String = "") {
        viewModelScope.launch {
            val currentState = _uiState.value
            val protocol = currentState.protocol ?: return@launch
            val session = currentState.session ?: return@launch

            _uiState.value = currentState.copy(isLoading = true)

            try {
                // Update session with response
                val updatedSession = repository.updateSession(
                    session,
                    currentState.currentStepIndex + 1,
                    userResponse
                )

                // Check if completed
                if (updatedSession.isCompleted) {
                    _uiState.value = currentState.copy(
                        session = updatedSession,
                        isCompleted = true,
                        isLoading = false
                    )
                    return@launch
                }

                // Move to next step
                val nextIndex = currentState.currentStepIndex + 1
                if (nextIndex < protocol.steps.size) {
                    _uiState.value = currentState.copy(
                        currentStep = protocol.steps[nextIndex],
                        currentStepIndex = nextIndex,
                        session = updatedSession,
                        isLoading = false
                    )
                } else {
                    // Completed
                    _uiState.value = currentState.copy(
                        session = updatedSession,
                        isCompleted = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Failed to proceed: ${e.message}"
                )
            }
        }
    }

    /**
     * Go to previous step
     */
    fun previousStep() {
        val currentState = _uiState.value
        val protocol = currentState.protocol ?: return

        val prevIndex = currentState.currentStepIndex - 1
        if (prevIndex >= 0) {
            _uiState.value = currentState.copy(
                currentStep = protocol.steps[prevIndex],
                currentStepIndex = prevIndex,
                error = null
            )
        }
    }

    /**
     * Handle step response (for questions/confirmations)
     */
    fun onStepResponse(response: String) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Check if response triggers escalation
            val shouldEscalate = repository.shouldEscalate(response, emptyList())

            if (shouldEscalate) {
                val escalation = Escalation(
                    reason = "User response indicated need for medical attention: $response",
                    level = EscalationLevel.MEDIUM,//here 这里的medium是固定的吗？
                    contactRequired = true,
                    urgency = "Please contact healthcare provider",
                    instructions = "A healthcare professional should review your situation."
                )

                _uiState.value = currentState.copy(
                    shouldEscalate = true,
                    escalation = escalation
                )
            } else {
                // Continue to next step
                nextStep(response)
            }
        }
    }

    /**
     * Skip current step (if allowed)
     */
    fun skipStep() {
        val currentState = _uiState.value
        val currentStep = currentState.currentStep

        // Only allow skipping non-required steps
        if (currentStep?.requiresConfirmation != true) {
            nextStep("[Skipped]")
        }
    }

    /**
     * Reset onboarding
     */
    fun reset() {
        _uiState.value = OnboardingUiState()
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Get progress percentage
     */
    fun getProgress(): Float {
        val state = _uiState.value
        if (state.totalSteps == 0) return 0f
        return (state.currentStepIndex + 1).toFloat() / state.totalSteps.toFloat()
    }
}