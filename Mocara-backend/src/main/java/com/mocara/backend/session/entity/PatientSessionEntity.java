package com.mocara.backend.session.entity;

import com.mocara.backend.protocol.entity.ProtocolEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patient_sessions")
public class PatientSessionEntity {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "drug_id", nullable = false)
    private String drugId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id")
    private ProtocolEntity protocol;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "is_escalated", nullable = false)
    private boolean escalated;

    @Column(name = "start_time_ms", nullable = false)
    private long startTimeMs;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionResponseEntity> responses = new ArrayList<>();

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private EscalationEntity escalation;

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public ProtocolEntity getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolEntity protocol) {
        this.protocol = protocol;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public List<SessionResponseEntity> getResponses() {
        return responses;
    }

    public void setResponses(List<SessionResponseEntity> responses) {
        this.responses = responses;
    }

    public EscalationEntity getEscalation() {
        return escalation;
    }

    public void setEscalation(EscalationEntity escalation) {
        this.escalation = escalation;
    }
}

