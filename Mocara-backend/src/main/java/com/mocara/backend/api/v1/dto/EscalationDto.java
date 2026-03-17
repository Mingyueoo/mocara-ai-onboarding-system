package com.mocara.backend.api.v1.dto;

import com.mocara.backend.common.enums.EscalationLevel;

public record EscalationDto(
        String reason,
        EscalationLevel level,
        long timestamp,
        boolean contactRequired,
        String urgency,
        String instructions
) {}

