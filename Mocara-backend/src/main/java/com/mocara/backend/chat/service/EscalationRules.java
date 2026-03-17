package com.mocara.backend.chat.service;

import com.mocara.backend.api.v1.dto.ChatMessageDto;
import com.mocara.backend.common.enums.EscalationLevel;
import com.mocara.backend.common.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mirrors Android's MockChatResponses escalation rules.
 */
@Component
public class EscalationRules {

    private static final List<String> EMERGENCY_KEYWORDS = List.of(
            "overdose", "emergency", "911", "chest pain", "can't breathe",
            "severe pain", "unconscious", "seizure", "allergic reaction"
    );

    private static final List<String> URGENT_KEYWORDS = List.of(
            "very sick", "high fever", "persistent vomiting", "severe headache",
            "blurred vision", "numbness", "difficulty swallowing", "blood"
    );

    private static final List<String> CONCERN_KEYWORDS = List.of(
            "worried", "scared", "confused about dose", "missed several doses",
            "unusual symptoms", "not sure", "feeling worse"
    );

    public boolean shouldEscalate(String input, List<ChatMessageDto> context) {
        String lower = safeLower(input);
        if (EMERGENCY_KEYWORDS.stream().anyMatch(lower::contains)) return true;
        if (URGENT_KEYWORDS.stream().anyMatch(lower::contains)) return true;

        long concernCount = (context == null ? List.<ChatMessageDto>of() : context).stream()
                .filter(m -> m != null && m.role() != UserRole.SYSTEM)
                .filter(m -> {
                    String c = safeLower(m.content());
                    return CONCERN_KEYWORDS.stream().anyMatch(c::contains);
                })
                .count();
        return concernCount >= 2;
    }

    public EscalationLevel escalationLevel(String input) {
        String lower = safeLower(input);
        if (EMERGENCY_KEYWORDS.stream().anyMatch(lower::contains)) return EscalationLevel.CRITICAL;
        if (URGENT_KEYWORDS.stream().anyMatch(lower::contains)) return EscalationLevel.HIGH;
        if (CONCERN_KEYWORDS.stream().anyMatch(lower::contains)) return EscalationLevel.MEDIUM;
        return EscalationLevel.LOW;
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}

