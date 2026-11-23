package com.hlala.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "rent_payment")
public class RentPayment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID leaseId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.PaymentMethod method;

    @Column(nullable = false)
    private YearMonth coveredPeriod;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(nullable = false)
    private UUID recordedByUserId;

    protected RentPayment() {
    }

    public RentPayment(UUID id, UUID leaseId, BigDecimal amount, Enums.PaymentMethod method,
                        YearMonth coveredPeriod, Instant recordedAt, UUID recordedByUserId) {
        this.id = id;
        this.leaseId = leaseId;
        this.amount = amount;
        this.method = method;
        this.coveredPeriod = coveredPeriod;
        this.recordedAt = recordedAt;
        this.recordedByUserId = recordedByUserId;
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

    public Enums.PaymentMethod getMethod() {
        return method;
    }

    public YearMonth getCoveredPeriod() {
        return coveredPeriod;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public UUID getRecordedByUserId() {
        return recordedByUserId;
    }
}
