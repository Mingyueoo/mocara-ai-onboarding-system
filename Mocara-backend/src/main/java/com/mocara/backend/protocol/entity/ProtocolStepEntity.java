package com.mocara.backend.protocol.entity;

import com.mocara.backend.common.enums.StepType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "protocol_steps",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_protocol_steps_protocol_stepnum",
                columnNames = {"protocol_id", "step_number"}
        )
)
public class ProtocolStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id")
    private ProtocolEntity protocol;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private StepType type;

    @Column(name = "requires_confirmation", nullable = false)
    private boolean requiresConfirmation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", columnDefinition = "jsonb")
    private String optionsJson;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "confirmation_items_json", columnDefinition = "jsonb")
    private String confirmationItemsJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProtocolEntity getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolEntity protocol) {
        this.protocol = protocol;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public StepType getType() {
        return type;
    }

    public void setType(StepType type) {
        this.type = type;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public String getConfirmationItemsJson() {
        return confirmationItemsJson;
    }

    public void setConfirmationItemsJson(String confirmationItemsJson) {
        this.confirmationItemsJson = confirmationItemsJson;
    }
}

