package com.hlala.domain;

import jakarta.persistence.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A checker's in-field verification visit. Construct only via RoomCheckFactory. */
@Entity
@Table(name = "room_check")
public class RoomCheck {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID listingId;

    @Column(nullable = false)
    private UUID checkerId;

    @Column(nullable = false)
    private boolean passed;

    // EAGER + @OrderColumn: read after the transaction ends, and Hibernate refuses
    // two EAGER unordered lists ("bags") on one entity.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_check_result", joinColumns = @JoinColumn(name = "room_check_id"))
    @OrderColumn(name = "position")
    private List<ChecklistItemResult> results;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_check_photo", joinColumns = @JoinColumn(name = "room_check_id"))
    @OrderColumn(name = "position")
    @Column(name = "photo_uri")
    private List<URI> photoRefs;

    @Embedded
    private GeoPoint checkedAtLocation;

    @Column(nullable = false, updatable = false)
    private Instant completedAt;

    protected RoomCheck() {
    }

    public RoomCheck(UUID id, UUID listingId, UUID checkerId, boolean passed,
                      List<ChecklistItemResult> results, List<URI> photoRefs,
                      GeoPoint checkedAtLocation, Instant completedAt) {
        this.id = id;
        this.listingId = listingId;
        this.checkerId = checkerId;
        this.passed = passed;
        this.results = results;
        this.photoRefs = photoRefs;
        this.checkedAtLocation = checkedAtLocation;
        this.completedAt = completedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getListingId() {
        return listingId;
    }

    public UUID getCheckerId() {
        return checkerId;
    }

    public boolean isPassed() {
        return passed;
    }

    public List<ChecklistItemResult> getResults() {
        return results;
    }

    public List<URI> getPhotoRefs() {
        return photoRefs;
    }

    public GeoPoint getCheckedAtLocation() {
        return checkedAtLocation;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
