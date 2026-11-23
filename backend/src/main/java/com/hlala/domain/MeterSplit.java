package com.hlala.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Keys off Lease, not Room - a Room can have more than one concurrent Lease
 * when Room.occupancyMode is SHARED, and each lease's share of a top-up is
 * computed independently by MeterSplitStrategy.
 */
@Entity
@Table(name = "meter_split")
public class MeterSplit {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID meterReadingId;

    @Column(nullable = false)
    private UUID leaseId;

    @Column(nullable = false)
    private BigDecimal shareAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.SplitMode mode;

    protected MeterSplit() {
    }

    public MeterSplit(UUID id, UUID meterReadingId, UUID leaseId, BigDecimal shareAmount, Enums.SplitMode mode) {
        this.id = id;
        this.meterReadingId = meterReadingId;
        this.leaseId = leaseId;
        this.shareAmount = shareAmount;
        this.mode = mode;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMeterReadingId() {
        return meterReadingId;
    }

    public UUID getLeaseId() {
        return leaseId;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public Enums.SplitMode getMode() {
        return mode;
    }
}
