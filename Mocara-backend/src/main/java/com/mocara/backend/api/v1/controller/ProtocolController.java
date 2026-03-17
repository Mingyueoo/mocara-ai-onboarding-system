package com.mocara.backend.api.v1.controller;

import com.mocara.backend.api.v1.dto.ProtocolDto;
import com.mocara.backend.protocol.service.ProtocolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/protocols")
public class ProtocolController {

    private final ProtocolService protocolService;

    public ProtocolController(ProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    @GetMapping("/{drugId}")
    public ProtocolDto getProtocol(@PathVariable String drugId) {
        return protocolService.getProtocolByDrugId(drugId);
    }
}

