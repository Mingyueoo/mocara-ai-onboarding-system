package com.mocara.backend.api.v1.controller;

import com.mocara.backend.api.v1.dto.CreateSessionRequestDto;
import com.mocara.backend.api.v1.dto.PatientSessionDto;
import com.mocara.backend.api.v1.dto.UpdateStepRequestDto;
import com.mocara.backend.session.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public PatientSessionDto createSession(@Valid @RequestBody CreateSessionRequestDto request) {
        return sessionService.createSession(request.drugId(), request.protocolId());
    }

    @PutMapping("/{sessionId}/steps/{stepNumber}")
    public PatientSessionDto updateStep(
            @PathVariable UUID sessionId,
            @PathVariable int stepNumber,
            @Valid @RequestBody UpdateStepRequestDto request
    ) {
        return sessionService.updateSession(sessionId, stepNumber, request.response());
    }
}

