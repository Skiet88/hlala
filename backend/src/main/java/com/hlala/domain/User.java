package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Base identity. Role-specific data lives in the matching *Profile entity,
 * created only when that role is taken on - a user can hold more than one
 * role over time (e.g. the same phone lists a room and also rents one).
 *
 * Construct only via UserFactory.
 */
@Entity
@Table(name = "app_user")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean phoneVerified;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {
        // JPA only
    }

    public User(UUID id, String phone, String name, boolean phoneVerified, Instant createdAt) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.phoneVerified = phoneVerified;
        this.createdAt = createdAt;
    }

    /** The only mutation this entity allows - flips once, on OTP confirmation. */
    public User withPhoneVerified() {
        return new User(this.id, this.phone, this.name, true, this.createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
