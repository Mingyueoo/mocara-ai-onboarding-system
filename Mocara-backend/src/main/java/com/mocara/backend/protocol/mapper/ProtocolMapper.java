package com.mocara.backend.protocol.mapper;

import com.mocara.backend.api.v1.dto.ProtocolDto;
import com.mocara.backend.api.v1.dto.ProtocolStepDto;
import com.mocara.backend.common.json.JsonListCodec;
import com.mocara.backend.protocol.entity.ProtocolEntity;
import com.mocara.backend.protocol.entity.ProtocolStepEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProtocolMapper {

    private final JsonListCodec jsonListCodec;

    public ProtocolMapper(JsonListCodec jsonListCodec) {
        this.jsonListCodec = jsonListCodec;
    }

    public ProtocolDto toDto(ProtocolEntity entity) {
        List<ProtocolStepDto> steps = entity.getSteps().stream().map(this::toDto).toList();
        return new ProtocolDto(
                entity.getId(),
                entity.getDrugId(),
                entity.getDrugName(),
                entity.getIntent(),
                steps,
                entity.getDescription()
        );
    }

    public ProtocolStepDto toDto(ProtocolStepEntity step) {
        return new ProtocolStepDto(
                step.getStepNumber(),
                step.getTitle(),
                step.getContent(),
                step.getType(),
                jsonListCodec.fromJson(step.getOptionsJson()),
                step.isRequiresConfirmation(),
                jsonListCodec.fromJson(step.getConfirmationItemsJson())
        );
    }
}

