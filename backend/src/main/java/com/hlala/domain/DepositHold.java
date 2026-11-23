package com.hlala.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The escrow object. State changes ONLY via EscrowService.attemptTransition -
 * never by setting this field directly from a controller or any other service.
 * History (who/when/why for every transition) is reconstructed from
 * DepositHoldEventRepository.findByDepositHoldId, not stored inline here, so
 * the hold row itself stays small and the event log stays genuinely append-only.
 *
 * Construct only via DepositHoldFactory.
 */
@Entity
@Table(name = "deposit_hold")
public class DepositHold {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID leaseId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.DepositHoldState state;

    /** Set once, when the tenant confirms the keys were handed back at move-out. */
    private Instant handoverConfirmedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected DepositHold() {
    }

    public DepositHold(UUID id, UUID leaseId, BigDecimal amount, Enums.DepositHoldState state,
                        Instant handoverConfirmedAt, Instant createdAt) {
        this.id = id;
        this.leaseId = leaseId;
        this.amount = amount;
        this.state = state;
        this.handoverConfirmedAt = handoverConfirmedAt;
        this.createdAt = createdAt;
    }

    /**
     * Returns a new DepositHold with the same id/lease/amount/createdAt but the
     * given state. Used exclusively by EscrowService's transition machinery -
     * saving the result is what makes the state change durable.
     */
    public DepositHold withState(Enums.DepositHoldState newState) {
        return new DepositHold(id, leaseId, amount, newState, handoverConfirmedAt, createdAt);
    }

    public DepositHold withHandoverConfirmed(Instant at) {
        return new DepositHold(id, leaseId, amount, state, at, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeaseId() {
        return leaseId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Enums.DepositHoldState getState() {
        return state;
    }

    public Instant getHandoverConfirmedAt() {
        return handoverConfirmedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
