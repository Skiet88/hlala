package com.hlala.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The published, priced instance of a Room. Separated from Room because a room
 * can be relisted (new price, fresh verification) after a tenant moves out
 * without redoing the physical details.
 *
 * status and verificationStatus are independent axes on purpose: a Listing can
 * go LIVE while still UNVERIFIED (see ListPublish's "let the checker do it"
 * flow) - a RoomCheck is a badge, not a publish gate.
 *
 * Construct only via ListingFactory.
 */
@Entity
@Table(name = "listing")
public class Listing {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private BigDecimal rentAmount;

    @Column(nullable = false)
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ListingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.VerificationStatus verificationStatus;

    private Instant publishedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Listing() {
    }

    public Listing(UUID id, UUID roomId, BigDecimal rentAmount, BigDecimal depositAmount,
                   Enums.ListingStatus status, Enums.VerificationStatus verificationStatus,
                   Instant publishedAt, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.rentAmount = rentAmount;
        this.depositAmount = depositAmount;
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
    }

    public Listing published(Instant at) {
        return new Listing(id, roomId, rentAmount, depositAmount,
                Enums.ListingStatus.LIVE, verificationStatus, at, createdAt);
    }

    public Listing withVerificationStatus(Enums.VerificationStatus newStatus) {
        return new Listing(id, roomId, rentAmount, depositAmount, status, newStatus, publishedAt, createdAt);
    }

    public Listing let() {
        return new Listing(id, roomId, rentAmount, depositAmount,
                Enums.ListingStatus.LET, verificationStatus, publishedAt, createdAt);
    }

    public Listing archived() {
        return new Listing(id, roomId, rentAmount, depositAmount,
                Enums.ListingStatus.ARCHIVED, verificationStatus, publishedAt, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public BigDecimal getRentAmount() {
        return rentAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public Enums.ListingStatus getStatus() {
        return status;
    }

    public Enums.VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
