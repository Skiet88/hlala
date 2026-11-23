package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "repair_ticket")
public class RepairTicket {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID leaseId;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.RepairStatus status;

    @Column(nullable = false, updatable = false)
    private Instant reportedAt;

    protected RepairTicket() {
    }

    public RepairTicket(UUID id, UUID leaseId, String description, Enums.RepairStatus status,
                         Instant reportedAt) {
        this.id = id;
        this.leaseId = leaseId;
        this.description = description;
        this.status = status;
        this.reportedAt = reportedAt;
    }

    public RepairTicket withStatus(Enums.RepairStatus newStatus) {
        return new RepairTicket(id, leaseId, description, newStatus, reportedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeaseId() {
        return leaseId;
    }

    public String getDescription() {
        return description;
    }

    public Enums.RepairStatus getStatus() {
        return status;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }
}
