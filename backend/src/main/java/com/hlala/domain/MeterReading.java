package com.hlala.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meter_reading")
public class MeterReading {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private BigDecimal topUpAmount;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt;

    protected MeterReading() {
    }

    public MeterReading(UUID id, UUID roomId, BigDecimal topUpAmount, Instant recordedAt) {
        this.id = id;
        this.roomId = roomId;
        this.topUpAmount = topUpAmount;
        this.recordedAt = recordedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public BigDecimal getTopUpAmount() {
        return topUpAmount;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
