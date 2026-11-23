package com.hlala.domain;

import jakarta.persistence.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reachable only from an ACTIVE DepositHold (see EscrowService) - a pre-move-in
 * disagreement is a refund/cancellation case, not a dispute case.
 * Construct only via DisputeFactory.
 */
@Entity
@Table(name = "dispute")
public class Dispute {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID depositHoldId;

    @Column(nullable = false)
    private UUID raisedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.DisputeStatus status;

    /** Null until resolved. */
    @Enumerated(EnumType.STRING)
    private Enums.DisputeResolution resolution;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dispute_evidence", joinColumns = @JoinColumn(name = "dispute_id"))
    @Column(name = "evidence_uri")
    private List<URI> evidenceRefs;

    @Column(nullable = false, updatable = false)
    private Instant openedAt;

    private Instant resolvedAt;

    protected Dispute() {
    }

    public Dispute(UUID id, UUID depositHoldId, UUID raisedByUserId, Enums.DisputeStatus status,
                    Enums.DisputeResolution resolution, List<URI> evidenceRefs, Instant openedAt,
                    Instant resolvedAt) {
        this.id = id;
        this.depositHoldId = depositHoldId;
        this.raisedByUserId = raisedByUserId;
        this.status = status;
        this.resolution = resolution;
        this.evidenceRefs = evidenceRefs;
        this.openedAt = openedAt;
        this.resolvedAt = resolvedAt;
    }

    public Dispute withEvidence(List<URI> more) {
        List<URI> combined = new java.util.ArrayList<>(evidenceRefs);
        combined.addAll(more);
        return new Dispute(id, depositHoldId, raisedByUserId, status, resolution,
                List.copyOf(combined), openedAt, resolvedAt);
    }

    public Dispute resolved(Enums.DisputeResolution resolution, Instant resolvedAt) {
        return new Dispute(id, depositHoldId, raisedByUserId, Enums.DisputeStatus.RESOLVED,
                resolution, List.copyOf(evidenceRefs), openedAt, resolvedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getDepositHoldId() {
        return depositHoldId;
    }

    public UUID getRaisedByUserId() {
        return raisedByUserId;
    }

    public Enums.DisputeStatus getStatus() {
        return status;
    }

    public Enums.DisputeResolution getResolution() {
        return resolution;
    }

    public List<URI> getEvidenceRefs() {
        return evidenceRefs;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
