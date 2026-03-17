package com.mocara.app.domain.model

/**
 * User Role Types
 */
enum class UserRole {
    PATIENT,
    AVATAR,
    SYSTEM
}

/**
 * Protocol Intent Types
 */
enum class ProtocolIntent {
    ONBOARDING,
    SAFETY,
    DISEASE
}

/**
 * Escalation Severity Levels
 */
enum class EscalationLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Protocol - Medication onboarding protocol
 */
data class Protocol(
    val id: String,
    val drugId: String,
    val drugName: String,
    val intent: ProtocolIntent,
    val steps: List<ProtocolStep>,
    val description: String = ""
)

/**
 * ProtocolStep - Individual step in onboarding
 */
data class ProtocolStep(
    val stepNumber: Int,
    val title: String,
    val content: String,
    val type: StepType = StepType.INFO,
    val options: List<String>? = null,
    val requiresConfirmation: Boolean = false,
    // 新增：需要用户逐条勾选确认的条目 Added: Items that require users to check off each one for confirmation.
    val confirmationItems: List<String>? = null
)

/**
 * Step Type
 */
enum class StepType {
    INFO,           // Information display
    QUESTION,       // Question with options
    CONFIRMATION    // Requires user confirmation
}

/**
 * ChatMessage - Message in chat
 */
data class ChatMessage(
    val id: String,
    val role: UserRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEscalated: Boolean = false,
    val avatarEmotion: AvatarEmotion = AvatarEmotion.NEUTRAL
)

/**
 * Avatar Emotion States
 */
enum class AvatarEmotion {
    NEUTRAL,
    HAPPY,
    CONCERNED,
    SERIOUS,
    EMPATHETIC
}

/**
 * PatientSession - User session data
 */
data class PatientSession(
    val sessionId: String,
    val drugId: String,
    val protocolId: String,
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val isEscalated: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val escalation: Escalation? = null,
    val responses: Map<Int, String> = emptyMap()
)

/**
 * Escalation - Escalation details
 */
data class Escalation(
    val reason: String,
    val level: EscalationLevel,
    val timestamp: Long = System.currentTimeMillis(),
    val contactRequired: Boolean = true,
    val urgency: String = "",
    val instructions: String = ""
)