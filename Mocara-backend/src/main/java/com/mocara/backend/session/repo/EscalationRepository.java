package com.mocara.backend.session.repo;

import com.mocara.backend.session.entity.EscalationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EscalationRepository extends JpaRepository<EscalationEntity, Long> {
    Optional<EscalationEntity> findBySessionSessionId(UUID sessionId);
}

