package com.mocara.backend.protocol.repo;

import com.mocara.backend.protocol.entity.ProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProtocolRepository extends JpaRepository<ProtocolEntity, String> {
    Optional<ProtocolEntity> findFirstByDrugIdIgnoreCase(String drugId);
}

