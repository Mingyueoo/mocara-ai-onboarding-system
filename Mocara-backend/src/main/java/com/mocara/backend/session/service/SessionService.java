package com.mocara.backend.session.service;

import com.mocara.backend.api.v1.dto.PatientSessionDto;
import com.mocara.backend.auth.entity.AuthRole;
import com.mocara.backend.auth.repo.AppUserRepository;
import com.mocara.backend.auth.security.AuthenticatedUser;
import com.mocara.backend.protocol.repo.ProtocolRepository;
import com.mocara.backend.session.entity.PatientSessionEntity;
import com.mocara.backend.session.entity.SessionResponseEntity;
import com.mocara.backend.session.mapper.SessionMapper;
import com.mocara.backend.session.repo.PatientSessionRepository;
import com.mocara.backend.session.repo.SessionResponseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionService {

    private final ProtocolRepository protocolRepository;
    private final PatientSessionRepository patientSessionRepository;
    private final SessionResponseRepository sessionResponseRepository;
    private final SessionMapper sessionMapper;
    private final AppUserRepository appUserRepository;

    public SessionService(
            ProtocolRepository protocolRepository,
            PatientSessionRepository patientSessionRepository,
            SessionResponseRepository sessionResponseRepository,
            SessionMapper sessionMapper,
            AppUserRepository appUserRepository
    ) {
        this.protocolRepository = protocolRepository;
        this.patientSessionRepository = patientSessionRepository;
        this.sessionResponseRepository = sessionResponseRepository;
        this.sessionMapper = sessionMapper;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public PatientSessionDto createSession(String drugId, String protocolId, AuthenticatedUser currentUser) {
        var protocol = protocolRepository.findById(protocolId)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));
        var owner = appUserRepository.findById(currentUser.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUser.userId()));

        PatientSessionEntity session = new PatientSessionEntity();
        session.setSessionId(UUID.randomUUID());
        session.setDrugId(drugId);
        session.setProtocol(protocol);
        session.setUser(owner);
        session.setCurrentStep(0);
        session.setCompleted(false);
        session.setEscalated(false);
        session.setStartTimeMs(System.currentTimeMillis());

        PatientSessionEntity saved = patientSessionRepository.save(session);
        return sessionMapper.toDto(saved);
    }

    @Transactional
    public PatientSessionDto updateSession(UUID sessionId, int stepNumber, String response, AuthenticatedUser currentUser) {
        var session = patientSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        validateOwnership(session, currentUser);

        var protocol = session.getProtocol();
        protocol.getSteps().size();
        boolean isLastStep = protocol.getSteps().size() == stepNumber;

        SessionResponseEntity entity = sessionResponseRepository
                .findBySessionSessionIdAndStepNumber(sessionId, stepNumber)
                .orElseGet(SessionResponseEntity::new);

        entity.setSession(session);
        entity.setStepNumber(stepNumber);
        entity.setResponse(response);
        entity.setCreatedAtMs(System.currentTimeMillis());
        sessionResponseRepository.save(entity);

        session.setCurrentStep(stepNumber);
        session.setCompleted(isLastStep);

        var saved = patientSessionRepository.save(session);
//        saved.setResponses(sessionResponseRepository.findBySessionSessionId(sessionId));//500 Internal Server Error

        return sessionMapper.toDto(saved);
    }

    private void validateOwnership(PatientSessionEntity session, AuthenticatedUser user) {
        if (user.roles().contains(AuthRole.ADMIN)) {
            return;
        }
        if (session.getUser() == null || !session.getUser().getId().equals(user.userId())) {
            throw new IllegalArgumentException("You cannot access another user's session");
        }
    }
}

