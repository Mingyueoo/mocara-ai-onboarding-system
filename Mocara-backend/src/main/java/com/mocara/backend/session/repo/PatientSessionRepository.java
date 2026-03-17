package com.mocara.backend.session.repo;

import com.mocara.backend.session.entity.PatientSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientSessionRepository extends JpaRepository<PatientSessionEntity, UUID> {}

