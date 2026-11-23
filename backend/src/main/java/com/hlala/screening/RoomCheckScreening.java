package com.hlala.screening;

import com.hlala.domain.ChecklistItemResult;
import com.hlala.domain.GeoPoint;
import com.hlala.domain.Room;

import java.net.URI;
import java.util.List;

/**
 * Rules a checker's submission must meet, separate from what the checker
 * ticked. Returns extra checklist lines (e.g. "Location match: failed") that
 * are stored alongside the checker's own results, so a failed screen is
 * visible in the record rather than a silent rejection.
 */
public interface RoomCheckScreening {

    List<ChecklistItemResult> screen(Room room, List<ChecklistItemResult> checkerResults,
                                     List<URI> photoRefs, GeoPoint checkedAt);
}
