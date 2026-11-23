package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "viewing")
public class Viewing {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID listingId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private Instant scheduledFor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ViewingStatus status;

    protected Viewing() {
    }

    public Viewing(UUID id, UUID listingId, UUID tenantId, Instant scheduledFor, Enums.ViewingStatus status) {
        this.id = id;
        this.listingId = listingId;
        this.tenantId = tenantId;
        this.scheduledFor = scheduledFor;
        this.status = status;
    }

    public Viewing withStatus(Enums.ViewingStatus newStatus) {
        return new Viewing(id, listingId, tenantId, scheduledFor, newStatus);
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

    public Instant getScheduledFor() {
        return scheduledFor;
    }

    public Enums.ViewingStatus getStatus() {
        return status;
    }
}
