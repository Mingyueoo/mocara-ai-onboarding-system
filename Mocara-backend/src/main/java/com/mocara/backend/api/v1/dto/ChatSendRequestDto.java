package com.mocara.backend.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ChatSendRequestDto(
        @NotNull UUID sessionId,
        @NotBlank String input,
        List<ChatMessageDto> context
) {}

