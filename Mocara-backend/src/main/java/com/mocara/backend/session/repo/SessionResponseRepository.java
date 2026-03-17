package com.mocara.backend.session.repo;

import com.mocara.backend.session.entity.SessionResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionResponseRepository extends JpaRepository<SessionResponseEntity, Long> {
    List<SessionResponseEntity> findBySessionSessionId(UUID sessionId);
    Optional<SessionResponseEntity> findBySessionSessionIdAndStepNumber(UUID sessionId, int stepNumber);
}

