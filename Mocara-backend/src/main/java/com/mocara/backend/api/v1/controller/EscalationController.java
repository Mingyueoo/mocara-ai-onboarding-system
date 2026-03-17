package com.mocara.backend.api.v1.controller;

import com.mocara.backend.api.v1.dto.EscalationCheckRequestDto;
import com.mocara.backend.api.v1.dto.EscalationCheckResponseDto;
import com.mocara.backend.session.service.EscalationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/escalations")
public class EscalationController {

    private final EscalationService escalationService;

    public EscalationController(EscalationService escalationService) {
        this.escalationService = escalationService;
    }

    @PostMapping("/check")
    public EscalationCheckResponseDto check(@Valid @RequestBody EscalationCheckRequestDto request) {
        return escalationService.check(request.input(), request.context());
    }
}

