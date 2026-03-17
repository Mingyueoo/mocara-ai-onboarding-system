package com.mocara.backend.chat.service;

import com.mocara.backend.api.v1.dto.ChatMessageDto;
import com.mocara.backend.common.enums.AvatarEmotion;
import com.mocara.backend.common.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mirrors Android's MockChatResponses.generateResponse().
 */
@Component
public class MockChatResponder {

    private final EscalationRules escalationRules;

    public MockChatResponder(EscalationRules escalationRules) {
        this.escalationRules = escalationRules;
    }

    public ChatMessageDto generateResponse(String input, String drugId, List<ChatMessageDto> context) {
        if (escalationRules.shouldEscalate(input, context)) {
            return createEscalationMessage(input);
        }

        String lower = safeLower(input);
        String response;
        if (lower.contains("dose") || lower.contains("how much")) {
            response =
                    "I can provide general dosing information, but your specific dose should be determined by your healthcare provider. " +
                            "For Ozempic, the typical starting dose is 0.25 mg once weekly for 4 weeks, then 0.5 mg. " +
                            "Always follow your doctor's instructions. Do you have any other questions?";
        } else if (lower.contains("side effect") || lower.contains("nausea")) {
            response =
                    "Common side effects of Ozempic include nausea, vomiting, diarrhea, and constipation. " +
                            "These often improve after the first few weeks as your body adjusts. " +
                            "Taking the medication with food may help. If side effects are severe or don't improve, please contact your doctor.";
        } else if (lower.contains("missed") || lower.contains("forgot")) {
            response =
                    "If you miss a dose of Ozempic:\n" +
                            "• If less than 5 days have passed, take it as soon as you remember\n" +
                            "• If more than 5 days have passed, skip the missed dose and take your next dose on the regularly scheduled day\n" +
                            "• Never double up on doses\n\n" +
                            "Would you like to set up reminders to help you remember?";
        } else if (lower.contains("store") || lower.contains("refrigerate")) {
            response =
                    "Store unopened Ozempic pens in the refrigerator at 36°F to 46°F (2°C to 8°C). " +
                            "After first use, you can keep it in the refrigerator or at room temperature (up to 86°F/30°C) for up to 56 days. " +
                            "Never freeze insulin or Ozempic. Keep it away from direct heat and light.";
        } else if (lower.contains("inject") || lower.contains("site")) {
            response =
                    "You can inject Ozempic in three areas:\n" +
                            "• Abdomen (avoid 2 inches around your navel)\n" +
                            "• Front or side of thighs\n" +
                            "• Back of upper arms\n\n" +
                            "Rotate sites each week to prevent irritation. Clean the site with alcohol before injecting.";
        } else if (lower.contains("weight") || lower.contains("lose")) {
            response =
                    "While weight loss can be a side effect of Ozempic, it's primarily prescribed for managing type 2 diabetes. " +
                            "Any weight changes should be discussed with your healthcare provider. " +
                            "Maintaining a healthy diet and exercise routine is important alongside medication.";
        } else if (lower.contains("hello") || lower.contains("hi")) {
            response =
                    "Hello! I'm here to help answer your questions about your medication. " +
                            "What would you like to know?";
        } else if (lower.contains("thank")) {
            response = "You're welcome! Is there anything else you'd like to know about your medication?";
        } else {
            response =
                    "I understand you're asking about " + ((input != null && input.length() > 50) ? "your medication" : String.valueOf(input)) + ". " +
                            "Could you please provide more specific details? For example:\n" +
                            "• Questions about dosing\n" +
                            "• Side effects you're experiencing\n" +
                            "• Storage instructions\n" +
                            "• How to inject properly";
        }

        return new ChatMessageDto(
                UUID.randomUUID(),
                UserRole.AVATAR,
                response,
                System.currentTimeMillis(),
                false,
                determineEmotion(input)
        );
    }

    public ChatMessageDto greeting(String drugName) {
        return new ChatMessageDto(
                UUID.randomUUID(),
                UserRole.AVATAR,
                "Hello! I'm here to help answer your questions about " + drugName + ". " +
                        "Feel free to ask me about dosing, side effects, storage, or how to use your medication safely. " +
                        "How can I assist you today?",
                System.currentTimeMillis(),
                false,
                AvatarEmotion.HAPPY
        );
    }

    private ChatMessageDto createEscalationMessage(String input) {
        var level = escalationRules.escalationLevel(input);
        String content = switch (level) {
            case CRITICAL -> "⚠️ This sounds like a medical emergency. Please call 911 or go to the nearest emergency room immediately. " +
                    "Do not wait for a callback.";
            case HIGH -> "⚠️ I'm concerned about what you're describing. This requires immediate medical attention. " +
                    "Please contact your healthcare provider right away or visit urgent care.";
            case MEDIUM -> "I want to make sure you get the best care. Your situation would benefit from speaking with a healthcare professional. " +
                    "I'm going to connect you with our medical team.";
            default -> "For this type of question, it's best to speak with a healthcare professional who can provide personalized guidance.";
        };

        return new ChatMessageDto(
                UUID.randomUUID(),
                UserRole.AVATAR,
                content,
                System.currentTimeMillis(),
                true,
                AvatarEmotion.SERIOUS
        );
    }

    private static AvatarEmotion determineEmotion(String input) {
        String lower = safeLower(input);
        if (lower.contains("overdose") || lower.contains("emergency") || lower.contains("911") || lower.contains("can't breathe")) {
            return AvatarEmotion.SERIOUS;
        }
        if (lower.contains("very sick") || lower.contains("high fever") || lower.contains("persistent vomiting")) {
            return AvatarEmotion.CONCERNED;
        }
        if (lower.contains("worried") || lower.contains("scared") || lower.contains("not sure")) {
            return AvatarEmotion.EMPATHETIC;
        }
        if (lower.contains("thank") || lower.contains("good")) {
            return AvatarEmotion.HAPPY;
        }
        return AvatarEmotion.NEUTRAL;
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}

