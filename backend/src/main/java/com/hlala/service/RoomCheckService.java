package com.hlala.service;

import com.hlala.domain.ChecklistItemResult;
import com.hlala.domain.GeoPoint;
import com.hlala.domain.Listing;
import com.hlala.domain.RoomCheck;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public interface RoomCheckService {

    RoomCheck submit(Actor checker, UUID listingId, List<ChecklistItemResult> results, List<URI> photos,
                     GeoPoint checkedAt);

    List<Listing> queue(Actor checker);

    List<RoomCheck> mine(Actor checker);
}
