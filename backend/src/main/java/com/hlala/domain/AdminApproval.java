package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Its own entity, not a boolean flag on DepositHold, specifically so the
 * "two DISTINCT admins" rule can be enforced with
 * COUNT(DISTINCT adminProfileId) rather than trusting two flags not to be
 * set by the same person.
 */
@Entity
@Table(name = "admin_approval")
public class AdminApproval {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID depositHoldId;

    @Column(nullable = false)
    private UUID adminProfileId;

    @Column(nullable = false, updatable = false)
    private Instant approvedAt;

    protected AdminApproval() {
    }

    public AdminApproval(UUID id, UUID depositHoldId, UUID adminProfileId, Instant approvedAt) {
        this.id = id;
        this.depositHoldId = depositHoldId;
        this.adminProfileId = adminProfileId;
        this.approvedAt = approvedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDepositHoldId() {
        return depositHoldId;
    }

    public UUID getAdminProfileId() {
        return adminProfileId;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }
}
