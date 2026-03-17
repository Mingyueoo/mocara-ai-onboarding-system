package com.mocara.backend.api.v1.dto;

import com.mocara.backend.common.enums.StepType;

import java.util.List;

public record ProtocolStepDto(
        int stepNumber,
        String title,
        String content,
        StepType type,
        List<String> options,
        boolean requiresConfirmation,
        List<String> confirmationItems
) {}

