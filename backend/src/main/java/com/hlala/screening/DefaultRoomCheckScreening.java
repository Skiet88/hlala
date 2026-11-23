package com.hlala.screening;

import com.hlala.domain.ChecklistItemResult;
import com.hlala.domain.GeoPoint;
import com.hlala.domain.Room;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Three rules, matching the checker screen:
 *  1. The checker was physically at the pinned room (within MAX_DISTANCE_METERS).
 *  2. All six required photos were taken (door, room, window, toilet, yard, gate).
 *  3. Every required checklist item was answered.
 */
@Component
public class DefaultRoomCheckScreening implements RoomCheckScreening {

    static final double MAX_DISTANCE_METERS = 150;
    static final int REQUIRED_PHOTOS = 6;
    static final Set<String> REQUIRED_ITEMS = Set.of(
            "Room exists", "Landlord ID", "Toilet", "Meter", "Locks");

    @Override
    public List<ChecklistItemResult> screen(Room room, List<ChecklistItemResult> checkerResults,
                                            List<URI> photoRefs, GeoPoint checkedAt) {
        List<ChecklistItemResult> lines = new ArrayList<>();

        if (room.getPin() == null || checkedAt == null) {
            lines.add(new ChecklistItemResult("Location match", false, "No location recorded for the visit"));
        } else {
            double meters = room.getPin().distanceMetersTo(checkedAt);
            boolean near = meters <= MAX_DISTANCE_METERS;
            lines.add(new ChecklistItemResult("Location match", near,
                    near ? null : String.format("Visit was %.0f m from the pinned room", meters)));
        }

        int photos = photoRefs == null ? 0 : photoRefs.size();
        lines.add(new ChecklistItemResult("Required photos", photos >= REQUIRED_PHOTOS,
                photos >= REQUIRED_PHOTOS ? null : photos + " of " + REQUIRED_PHOTOS + " photos taken"));

        Set<String> answered = checkerResults.stream()
                .map(ChecklistItemResult::getItem)
                .collect(Collectors.toSet());
        Set<String> missing = REQUIRED_ITEMS.stream()
                .filter(item -> !answered.contains(item))
                .collect(Collectors.toSet());
        lines.add(new ChecklistItemResult("Checklist complete", missing.isEmpty(),
                missing.isEmpty() ? null : "Missing: " + String.join(", ", missing)));

        return lines;
    }
}
