package com.hlala.factory;

import com.hlala.domain.ChecklistItemResult;
import com.hlala.domain.GeoPoint;
import com.hlala.domain.RoomCheck;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class RoomCheckFactory {

    public RoomCheck create(UUID listingId, UUID checkerId, List<ChecklistItemResult> results,
                             List<URI> photoRefs, GeoPoint checkedAtLocation) {
        if (listingId == null) {
            throw new ValidationException("A room check must reference a listing");
        }
        if (checkerId == null) {
            throw new ValidationException("A room check must reference a checker");
        }
        if (results == null || results.isEmpty()) {
            throw new ValidationException("A room check must include checklist results");
        }
        boolean passed = results.stream().allMatch(ChecklistItemResult::isPassed);
        return new RoomCheck(UUID.randomUUID(), listingId, checkerId, passed, List.copyOf(results),
                photoRefs == null ? List.of() : List.copyOf(photoRefs), checkedAtLocation, Instant.now());
    }
}
