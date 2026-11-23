package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Tenant's formal application. Carries only the verification TICKS, never the
 * underlying documents - ApplicationDto mirrors this restriction at the API
 * boundary so the "landlord sees ticks only" privacy promise can't be broken
 * by a careless DTO change later.
 */
@Entity
@Table(name = "application")
public class Application {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID listingId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private boolean idVerified;

    @Column(nullable = false)
    private boolean incomeVerified;

    @Column(nullable = false)
    private boolean referencesChecked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ApplicationStatus status;

    @Column(nullable = false, updatable = false)
    private Instant appliedAt;

    protected Application() {
    }

    public Application(UUID id, UUID listingId, UUID tenantId, boolean idVerified, boolean incomeVerified,
                        boolean referencesChecked, Enums.ApplicationStatus status, Instant appliedAt) {
        this.id = id;
        this.listingId = listingId;
        this.tenantId = tenantId;
        this.idVerified = idVerified;
        this.incomeVerified = incomeVerified;
        this.referencesChecked = referencesChecked;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public Application withStatus(Enums.ApplicationStatus newStatus) {
        return new Application(id, listingId, tenantId, idVerified, incomeVerified,
                referencesChecked, newStatus, appliedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getListingId() {
        return listingId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public boolean isIdVerified() {
        return idVerified;
    }

    public boolean isIncomeVerified() {
        return incomeVerified;
    }

    public boolean isReferencesChecked() {
        return referencesChecked;
    }

    public Enums.ApplicationStatus getStatus() {
        return status;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }
}
