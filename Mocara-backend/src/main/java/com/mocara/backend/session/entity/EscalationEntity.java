package com.mocara.backend.session.entity;

import com.mocara.backend.common.enums.EscalationLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "escalations")
public class EscalationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", unique = true)
    private PatientSessionEntity session;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscalationLevel level;

    @Column(name = "timestamp_ms", nullable = false)
    private long timestampMs;

    @Column(name = "contact_required", nullable = false)
    private boolean contactRequired = true;

    @Column(nullable = false)
    private String urgency = "";

    @Column(nullable = false, columnDefinition = "text")
    private String instructions = "";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PatientSessionEntity getSession() {
        return session;
    }

    public void setSession(PatientSessionEntity session) {
        this.session = session;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public EscalationLevel getLevel() {
        return level;
    }

    public void setLevel(EscalationLevel level) {
        this.level = level;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public boolean isContactRequired() {
        return contactRequired;
    }

    public void setContactRequired(boolean contactRequired) {
        this.contactRequired = contactRequired;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}

