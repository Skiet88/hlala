package com.hlala.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Construct only via LeaseFactory. The link to the deposit runs one way:
 * DepositHold.leaseId -> Lease. Lease does not point back, which avoids a
 * circular create (each needing the other's id first).
 */
@Entity
@Table(name = "lease")
public class Lease {

    @Id
    private UUID id;

    /**
     * Optimistic lock. Every update checks it, so two requests that read the
     * same row cannot both write it; the second gets a conflict and retries.
     * Copies made by the with/resolved methods keep it, which is what makes
     * the check work with immutable entities.
     */
    @Version
    private Long version;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID landlordId;

    @Column(nullable = false)
    private BigDecimal monthlyRent;

    @Column(nullable = false)
    private LocalDate startDate;

    /** Nullable - open-ended tenancies are the norm in this market. */
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.LeaseLanguage language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.SignatureStatus tenantSignature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.SignatureStatus landlordSignature;

    protected Lease() {
    }

    public Lease(UUID id, UUID roomId, UUID tenantId, UUID landlordId,
                 BigDecimal monthlyRent, LocalDate startDate, LocalDate endDate,
                 Enums.LeaseLanguage language, Enums.SignatureStatus tenantSignature,
                 Enums.SignatureStatus landlordSignature) {
        this.id = id;
        this.roomId = roomId;
        this.tenantId = tenantId;
        this.landlordId = landlordId;
        this.monthlyRent = monthlyRent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.language = language;
        this.tenantSignature = tenantSignature;
        this.landlordSignature = landlordSignature;
    }

    public Lease withTenantSignature(Enums.SignatureStatus status) {
        return sameVersion(new Lease(id, roomId, tenantId, landlordId, monthlyRent,
                startDate, endDate, language, status, landlordSignature));
    }

    public Lease withLandlordSignature(Enums.SignatureStatus status) {
        return sameVersion(new Lease(id, roomId, tenantId, landlordId, monthlyRent,
                startDate, endDate, language, tenantSignature, status));
    }

    public boolean isFullySigned() {
        return tenantSignature == Enums.SignatureStatus.SIGNED
                && landlordSignature == Enums.SignatureStatus.SIGNED;
    }

    private Lease sameVersion(Lease copy) {
        copy.version = this.version;
        return copy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getLandlordId() {
        return landlordId;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Enums.LeaseLanguage getLanguage() {
        return language;
    }

    public Enums.SignatureStatus getTenantSignature() {
        return tenantSignature;
    }

    public Enums.SignatureStatus getLandlordSignature() {
        return landlordSignature;
    }
}
