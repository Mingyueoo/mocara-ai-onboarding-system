package com.mocara.backend.api.v1.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStepRequestDto(
        @NotNull String response
) {}

