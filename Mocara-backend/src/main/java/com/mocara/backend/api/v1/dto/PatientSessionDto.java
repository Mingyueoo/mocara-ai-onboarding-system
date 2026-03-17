package com.mocara.backend.api.v1.dto;

import java.util.Map;
import java.util.UUID;

public record PatientSessionDto(
        UUID sessionId,
        String drugId,
        String protocolId,
        int currentStep,
        boolean isCompleted,
        boolean isEscalated,
        long startTime,
        EscalationDto escalation,
        Map<Integer, String> responses
) {}

