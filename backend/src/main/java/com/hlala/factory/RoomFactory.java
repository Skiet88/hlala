package com.hlala.factory;

import com.hlala.domain.Enums;
import com.hlala.domain.GeoPoint;
import com.hlala.domain.Room;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
public class RoomFactory {

    public Room create(UUID landlordId, String area, String landmarkNote, GeoPoint pin,
                        Enums.RoomType type, Set<Enums.Amenity> amenities, Enums.OccupancyMode occupancyMode) {
        if (landlordId == null) {
            throw new ValidationException("A room must belong to a landlord");
        }
        if (area == null || area.isBlank()) {
            throw new ValidationException("Area is required");
        }
        if (type == null) {
            throw new ValidationException("Room type is required");
        }
        if (occupancyMode == null) {
            throw new ValidationException("Occupancy mode is required");
        }
        return new Room(UUID.randomUUID(), landlordId, area.trim(), landmarkNote, pin, type,
                amenities == null ? Set.of() : Set.copyOf(amenities), occupancyMode, Instant.now());
    }
}
