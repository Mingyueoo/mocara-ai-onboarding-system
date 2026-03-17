package com.mocara.backend.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequestDto(
        @NotBlank String drugId,
        @NotBlank String protocolId
) {}

