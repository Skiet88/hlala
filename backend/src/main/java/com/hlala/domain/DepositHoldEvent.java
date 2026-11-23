package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only audit row for every DepositHold state transition. Never updated,
 * never deleted - this IS the "Money trail" log in the admin back office.
 */
@Entity
@Table(name = "deposit_hold_event")
public class DepositHoldEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID depositHoldId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.DepositHoldState fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.DepositHoldState toState;

    @Column(nullable = false)
    private UUID triggeredByUserId;

    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected DepositHoldEvent() {
    }

    public DepositHoldEvent(UUID id, UUID depositHoldId, Enums.DepositHoldState fromState,
                             Enums.DepositHoldState toState, UUID triggeredByUserId, String reason,
                             Instant occurredAt) {
        this.id = id;
        this.depositHoldId = depositHoldId;
        this.fromState = fromState;
        this.toState = toState;
        this.triggeredByUserId = triggeredByUserId;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDepositHoldId() {
        return depositHoldId;
    }

    public Enums.DepositHoldState getFromState() {
        return fromState;
    }

    public Enums.DepositHoldState getToState() {
        return toState;
    }

    public UUID getTriggeredByUserId() {
        return triggeredByUserId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
