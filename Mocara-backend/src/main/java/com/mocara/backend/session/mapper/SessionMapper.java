package com.mocara.backend.session.mapper;

import com.mocara.backend.api.v1.dto.EscalationDto;
import com.mocara.backend.api.v1.dto.PatientSessionDto;
import com.mocara.backend.session.entity.EscalationEntity;
import com.mocara.backend.session.entity.PatientSessionEntity;
import com.mocara.backend.session.entity.SessionResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SessionMapper {

    public PatientSessionDto toDto(PatientSessionEntity entity) {
        Map<Integer, String> responses = entity.getResponses().stream()
                .collect(Collectors.toMap(SessionResponseEntity::getStepNumber, SessionResponseEntity::getResponse));

        return new PatientSessionDto(
                entity.getSessionId(),
                entity.getDrugId(),
                entity.getProtocol().getId(),
                entity.getCurrentStep(),
                entity.isCompleted(),
                entity.isEscalated(),
                entity.getStartTimeMs(),
                toDto(entity.getEscalation()),
                responses
        );
    }

    private EscalationDto toDto(EscalationEntity entity) {
        if (entity == null) return null;
        return new EscalationDto(
                entity.getReason(),
                entity.getLevel(),
                entity.getTimestampMs(),
                entity.isContactRequired(),
                entity.getUrgency(),
                entity.getInstructions()
        );
    }
}

