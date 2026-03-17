package com.mocara.backend.protocol.repo;

import com.mocara.backend.protocol.entity.ProtocolStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProtocolStepRepository extends JpaRepository<ProtocolStepEntity, Long> {
    List<ProtocolStepEntity> findByProtocolIdOrderByStepNumberAsc(String protocolId);
}

