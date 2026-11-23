package com.hlala.service;

import com.hlala.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ListingService {

    Room createRoom(Actor landlord, String area, String landmarkNote, GeoPoint pin, Enums.RoomType type,
                    Set<Enums.Amenity> amenities, Enums.OccupancyMode occupancyMode);

    Listing createListing(Actor landlord, UUID roomId, BigDecimal rent, BigDecimal deposit);

    /** requestCheck=true puts the listing in the checker queue (PENDING_CHECK); false goes live unverified. */
    Listing publish(Actor landlord, UUID listingId, boolean requestCheck);

    Listing archive(Actor landlord, UUID listingId);

    List<Listing> search(String area, BigDecimal maxRent, Enums.RoomType type, boolean verifiedOnly);

    Listing get(UUID listingId);

    /** Public view: only LIVE listings exist as far as the public is concerned. */
    Listing getPublic(UUID listingId);

    Room room(UUID roomId);

    List<Listing> forLandlord(Actor landlord);

    SavedListing save(Actor tenant, UUID listingId);

    List<SavedListing> saved(Actor tenant);

    Viewing bookViewing(Actor tenant, UUID listingId, Instant at);

    List<Viewing> viewingsForListing(Actor landlord, UUID listingId);
}
