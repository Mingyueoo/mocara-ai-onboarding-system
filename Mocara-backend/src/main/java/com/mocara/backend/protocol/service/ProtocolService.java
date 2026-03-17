package com.mocara.backend.protocol.service;

import com.mocara.backend.api.v1.dto.ProtocolDto;
import com.mocara.backend.protocol.mapper.ProtocolMapper;
import com.mocara.backend.protocol.repo.ProtocolRepository;
import org.springframework.stereotype.Service;

@Service
public class ProtocolService {

    private final ProtocolRepository protocolRepository;
    private final ProtocolMapper protocolMapper;

    public ProtocolService(ProtocolRepository protocolRepository, ProtocolMapper protocolMapper) {
        this.protocolRepository = protocolRepository;
        this.protocolMapper = protocolMapper;
    }

    public ProtocolDto getProtocolByDrugId(String drugId) {
        var entity = protocolRepository.findFirstByDrugIdIgnoreCase(drugId)
                .orElseThrow(() -> new IllegalArgumentException("Protocol not found for drugId=" + drugId));
        // ensure steps are loaded (JPA will load lazily; web request is transactional by default in Spring Boot 4)
        entity.getSteps().size();
        return protocolMapper.toDto(entity);
    }
}

