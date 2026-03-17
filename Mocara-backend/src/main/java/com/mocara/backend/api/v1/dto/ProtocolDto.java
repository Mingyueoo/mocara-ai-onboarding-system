package com.mocara.backend.api.v1.dto;

import com.mocara.backend.common.enums.ProtocolIntent;

import java.util.List;

public record ProtocolDto(
        String id,
        String drugId,
        String drugName,
        ProtocolIntent intent,
        List<ProtocolStepDto> steps,
        String description
) {}

