package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "landlord_profile")
public class LandlordProfile {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private Instant onboardedAt;

    protected LandlordProfile() {
    }

    public LandlordProfile(UUID id, UUID userId, Instant onboardedAt) {
        this.id = id;
        this.userId = userId;
        this.onboardedAt = onboardedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getOnboardedAt() {
        return onboardedAt;
    }
}
