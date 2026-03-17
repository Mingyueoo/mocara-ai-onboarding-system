package com.mocara.app.data.mock

import com.mocara.app.domain.model.*
import java.util.UUID

/**
 * Mock Chat Responses
 * Simulates AI responses and escalation logic
 * Future: Will be replaced by LLM API
 */
object MockChatResponses {

    // Escalation keywords
    private val emergencyKeywords = listOf(
        "overdose", "emergency", "911", "chest pain", "can't breathe",
        "severe pain", "unconscious", "seizure", "allergic reaction"
    )

    private val urgentKeywords = listOf(
        "very sick", "high fever", "persistent vomiting", "severe headache",
        "blurred vision", "numbness", "difficulty swallowing", "blood"
    )

    private val concernKeywords = listOf(
        "worried", "scared", "confused about dose", "missed several doses",
        "unusual symptoms", "not sure", "feeling worse"
    )

    /**
     * Check if input requires escalation
     */
    fun shouldEscalate(input: String, context: List<ChatMessage> = emptyList()): Boolean {
        val lowerInput = input.lowercase()

        // Emergency escalation
        if (emergencyKeywords.any { lowerInput.contains(it) }) {
            return true
        }

        // Urgent escalation
        if (urgentKeywords.any { lowerInput.contains(it) }) {
            return true
        }

        // Check for repeated concerns
        val concernCount = context.count {
            concernKeywords.any { keyword -> it.content.lowercase().contains(keyword) }
        }
        if (concernCount >= 2) {
            return true
        }

        return false
    }

    /**
     * Get escalation level based on input
     */
    fun getEscalationLevel(input: String): EscalationLevel {
        val lowerInput = input.lowercase()

        return when {
            emergencyKeywords.any { lowerInput.contains(it) } -> EscalationLevel.CRITICAL
            urgentKeywords.any { lowerInput.contains(it) } -> EscalationLevel.HIGH
            concernKeywords.any { lowerInput.contains(it) } -> EscalationLevel.MEDIUM
            else -> EscalationLevel.LOW
        }
    }

    /**
     * Generate mock AI response
     */
    fun generateResponse(
        input: String,
        drugId: String,
        context: List<ChatMessage> = emptyList()
    ): ChatMessage {
        val lowerInput = input.lowercase()

        // Check for escalation
        if (shouldEscalate(input, context)) {
            return createEscalationMessage(input)
        }

        // Generate appropriate response based on input
        val response = when {
            // Dosing questions
            lowerInput.contains("dose") || lowerInput.contains("how much") -> {
                "I can provide general dosing information, but your specific dose should be determined by your healthcare provider. " +
                        "For Ozempic, the typical starting dose is 0.25 mg once weekly for 4 weeks, then 0.5 mg. " +
                        "Always follow your doctor's instructions. Do you have any other questions?"
            }

            // Side effects
            lowerInput.contains("side effect") || lowerInput.contains("nausea") -> {
                "Common side effects of Ozempic include nausea, vomiting, diarrhea, and constipation. " +
                        "These often improve after the first few weeks as your body adjusts. " +
                        "Taking the medication with food may help. If side effects are severe or don't improve, please contact your doctor."
            }

            // Missed dose
            lowerInput.contains("missed") || lowerInput.contains("forgot") -> {
                "If you miss a dose of Ozempic:\n" +
                        "• If less than 5 days have passed, take it as soon as you remember\n" +
                        "• If more than 5 days have passed, skip the missed dose and take your next dose on the regularly scheduled day\n" +
                        "• Never double up on doses\n\n" +
                        "Would you like to set up reminders to help you remember?"
            }

            // Storage
            lowerInput.contains("store") || lowerInput.contains("refrigerate") -> {
                "Store unopened Ozempic pens in the refrigerator at 36°F to 46°F (2°C to 8°C). " +
                        "After first use, you can keep it in the refrigerator or at room temperature (up to 86°F/30°C) for up to 56 days. " +
                        "Never freeze insulin or Ozempic. Keep it away from direct heat and light."
            }

            // Injection site
            lowerInput.contains("inject") || lowerInput.contains("site") -> {
                "You can inject Ozempic in three areas:\n" +
                        "• Abdomen (avoid 2 inches around your navel)\n" +
                        "• Front or side of thighs\n" +
                        "• Back of upper arms\n\n" +
                        "Rotate sites each week to prevent irritation. Clean the site with alcohol before injecting."
            }

            // Weight loss
            lowerInput.contains("weight") || lowerInput.contains("lose") -> {
                "While weight loss can be a side effect of Ozempic, it's primarily prescribed for managing type 2 diabetes. " +
                        "Any weight changes should be discussed with your healthcare provider. " +
                        "Maintaining a healthy diet and exercise routine is important alongside medication."
            }

            // General greeting
            lowerInput.contains("hello") || lowerInput.contains("hi") -> {
                "Hello! I'm here to help answer your questions about your medication. " +
                        "What would you like to know?"
            }

            // Thanks
            lowerInput.contains("thank") -> {
                "You're welcome! Is there anything else you'd like to know about your medication?"
            }

            // Default response
            else -> {
                "I understand you're asking about ${if (input.length > 50) "your medication" else input}. " +
                        "Could you please provide more specific details? For example:\n" +
                        "• Questions about dosing\n" +
                        "• Side effects you're experiencing\n" +
                        "• Storage instructions\n" +
                        "• How to inject properly"
            }
        }

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            role = UserRole.AVATAR,
            content = response,
            avatarEmotion = determineEmotion(input)
        )
    }

    /**
     * Create escalation message
     */
    private fun createEscalationMessage(input: String): ChatMessage {
        val level = getEscalationLevel(input)

        val content = when (level) {
            EscalationLevel.CRITICAL -> {
                "⚠️ This sounds like a medical emergency. Please call 911 or go to the nearest emergency room immediately. " +
                        "Do not wait for a callback."
            }
            EscalationLevel.HIGH -> {
                "⚠️ I'm concerned about what you're describing. This requires immediate medical attention. " +
                        "Please contact your healthcare provider right away or visit urgent care."
            }
            EscalationLevel.MEDIUM -> {
                "I want to make sure you get the best care. Your situation would benefit from speaking with a healthcare professional. " +
                        "I'm going to connect you with our medical team."
            }
            else -> {
                "For this type of question, it's best to speak with a healthcare professional who can provide personalized guidance."
            }
        }

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            role = UserRole.AVATAR,
            content = content,
            isEscalated = true,
            avatarEmotion = AvatarEmotion.SERIOUS
        )
    }

    /**
     * Determine avatar emotion based on input
     */
    private fun determineEmotion(input: String): AvatarEmotion {
        val lowerInput = input.lowercase()

        return when {
            emergencyKeywords.any { lowerInput.contains(it) } -> AvatarEmotion.SERIOUS
            urgentKeywords.any { lowerInput.contains(it) } -> AvatarEmotion.CONCERNED
            concernKeywords.any { lowerInput.contains(it) } -> AvatarEmotion.EMPATHETIC
            lowerInput.contains("thank") || lowerInput.contains("good") -> AvatarEmotion.HAPPY
            else -> AvatarEmotion.NEUTRAL
        }
    }

    /**
     * Get greeting message based on time and context
     */
    fun getGreetingMessage(drugName: String): ChatMessage {
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            role = UserRole.AVATAR,
            content = "Hello! I'm here to help answer your questions about $drugName. " +
                    "Feel free to ask me about dosing, side effects, storage, or how to use your medication safely. " +
                    "How can I assist you today?",
            avatarEmotion = AvatarEmotion.HAPPY
        )
    }
}