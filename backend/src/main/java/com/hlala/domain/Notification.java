package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.NotificationType type;

    /** ID of the thing this notification is about (listing, dispute, repair ticket, etc.). */
    private String payloadRef;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(UUID id, UUID userId, Enums.NotificationType type, String payloadRef,
                         boolean read, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.payloadRef = payloadRef;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Notification markRead() {
        return new Notification(id, userId, type, payloadRef, true, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Enums.NotificationType getType() {
        return type;
    }

    public String getPayloadRef() {
        return payloadRef;
    }

    public boolean isRead() {
        return read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
