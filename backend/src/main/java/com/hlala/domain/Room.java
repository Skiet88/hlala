package com.hlala.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Physical unit - details that don't change between lettings. Construct only via RoomFactory. */
@Entity
@Table(name = "room")
public class Room {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID landlordId;

    @Column(nullable = false)
    private String area;

    private String landmarkNote;

    @Embedded
    private GeoPoint pin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.RoomType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_amenity", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity")
    private Set<Enums.Amenity> amenities = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.OccupancyMode occupancyMode;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Room() {
    }

    public Room(UUID id, UUID landlordId, String area, String landmarkNote, GeoPoint pin,
                Enums.RoomType type, Set<Enums.Amenity> amenities, Enums.OccupancyMode occupancyMode,
                Instant createdAt) {
        this.id = id;
        this.landlordId = landlordId;
        this.area = area;
        this.landmarkNote = landmarkNote;
        this.pin = pin;
        this.type = type;
        this.amenities = amenities;
        this.occupancyMode = occupancyMode;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLandlordId() {
        return landlordId;
    }

    public String getArea() {
        return area;
    }

    public String getLandmarkNote() {
        return landmarkNote;
    }

    public GeoPoint getPin() {
        return pin;
    }

    public Enums.RoomType getType() {
        return type;
    }

    public Set<Enums.Amenity> getAmenities() {
        return amenities;
    }

    public Enums.OccupancyMode getOccupancyMode() {
        return occupancyMode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
