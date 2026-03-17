package com.mocara.backend.protocol.entity;

import com.mocara.backend.common.enums.ProtocolIntent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocols")
public class ProtocolEntity {

    @Id
    private String id;

    @Column(name = "drug_id", nullable = false)
    private String drugId;

    @Column(name = "drug_name", nullable = false)
    private String drugName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProtocolIntent intent;

    @Column(nullable = false, columnDefinition = "text")
    private String description = "";

    @OneToMany(mappedBy = "protocol", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stepNumber ASC")
    private List<ProtocolStepEntity> steps = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public ProtocolIntent getIntent() {
        return intent;
    }

    public void setIntent(ProtocolIntent intent) {
        this.intent = intent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ProtocolStepEntity> getSteps() {
        return steps;
    }

    public void setSteps(List<ProtocolStepEntity> steps) {
        this.steps = steps;
    }
}

