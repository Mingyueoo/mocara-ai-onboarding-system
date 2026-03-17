package com.mocara.backend.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record EscalationCheckRequestDto(
        @NotBlank String input,
        List<ChatMessageDto> context
) {}