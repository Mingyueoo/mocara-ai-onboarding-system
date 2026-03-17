package com.mocara.backend.session.service;

import com.mocara.backend.api.v1.dto.ChatMessageDto;
import com.mocara.backend.api.v1.dto.EscalationCheckResponseDto;
import com.mocara.backend.chat.service.EscalationRules;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscalationService {

    private final EscalationRules escalationRules;

    public EscalationService(EscalationRules escalationRules) {
        this.escalationRules = escalationRules;
    }

    public EscalationCheckResponseDto check(String input, List<ChatMessageDto> context) {
        boolean should = escalationRules.shouldEscalate(input, context);
        return new EscalationCheckResponseDto(should);
    }
}

