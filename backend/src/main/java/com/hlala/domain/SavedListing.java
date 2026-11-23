package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saved_listing")
public class SavedListing {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID listingId;

    @Column(nullable = false, updatable = false)
    private Instant savedAt;

    protected SavedListing() {
    }

    public SavedListing(UUID id, UUID tenantId, UUID listingId, Instant savedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.listingId = listingId;
        this.savedAt = savedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getListingId() {
        return listingId;
    }

    public Instant getSavedAt() {
        return savedAt;
    }
}
