package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.ListingFactory;
import com.hlala.factory.RoomFactory;
import com.hlala.factory.ViewingFactory;
import com.hlala.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListingServiceImpl implements ListingService {

    private final RoomRepository rooms;
    private final ListingRepository listings;
    private final SavedListingRepository savedListings;
    private final ViewingRepository viewings;
    private final RoomFactory roomFactory;
    private final ListingFactory listingFactory;
    private final ViewingFactory viewingFactory;
    private final ProfileLookup lookup;
    private final NotificationService notifications;

    public ListingServiceImpl(RoomRepository rooms, ListingRepository listings,
                              SavedListingRepository savedListings, ViewingRepository viewings,
                              RoomFactory roomFactory, ListingFactory listingFactory,
                              ViewingFactory viewingFactory, ProfileLookup lookup,
                              NotificationService notifications) {
        this.rooms = rooms;
        this.listings = listings;
        this.savedListings = savedListings;
        this.viewings = viewings;
        this.roomFactory = roomFactory;
        this.listingFactory = listingFactory;
        this.viewingFactory = viewingFactory;
        this.lookup = lookup;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public Room createRoom(Actor landlord, String area, String landmarkNote, GeoPoint pin, Enums.RoomType type,
                           Set<Enums.Amenity> amenities, Enums.OccupancyMode occupancyMode) {
        LandlordProfile profile = lookup.landlord(landlord);
        return rooms.save(roomFactory.create(profile.getId(), area, landmarkNote, pin, type, amenities, occupancyMode));
    }

    @Override
    @Transactional
    public Listing createListing(Actor landlord, UUID roomId, BigDecimal rent, BigDecimal deposit) {
        Room room = room(roomId);
        lookup.requireLandlordOf(landlord, room);
        boolean hasOpenListing = listings.findByRoomId(roomId).stream()
                .anyMatch(l -> l.getStatus() == Enums.ListingStatus.DRAFT || l.getStatus() == Enums.ListingStatus.LIVE);
        if (hasOpenListing && room.getOccupancyMode() == Enums.OccupancyMode.SOLE) {
            throw new ValidationException("This room already has an open listing");
        }
        return listings.save(listingFactory.create(roomId, rent, deposit));
    }

    @Override
    @Transactional
    public Listing publish(Actor landlord, UUID listingId, boolean requestCheck) {
        Listing listing = ownedListing(landlord, listingId);
        if (listing.getStatus() != Enums.ListingStatus.DRAFT) {
            throw new ValidationException("Only a draft listing can be published");
        }
        Listing live = listing.published(Instant.now());
        if (requestCheck) {
            live = live.withVerificationStatus(Enums.VerificationStatus.PENDING_CHECK);
        }
        return listings.save(live);
    }

    @Override
    @Transactional
    public Listing archive(Actor landlord, UUID listingId) {
        return listings.save(ownedListing(landlord, listingId).archived());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Listing> search(String area, BigDecimal maxRent, Enums.RoomType type, boolean verifiedOnly) {
        List<Listing> live = listings.findByStatus(Enums.ListingStatus.LIVE);
        Map<UUID, Room> roomById = rooms.findAllById(live.stream().map(Listing::getRoomId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Room::getId, Function.identity()));

        String areaNeedle = area == null ? null : area.trim().toLowerCase(Locale.ROOT);
        return live.stream()
                .filter(l -> maxRent == null || l.getRentAmount().compareTo(maxRent) <= 0)
                .filter(l -> !verifiedOnly || l.getVerificationStatus() == Enums.VerificationStatus.VERIFIED)
                .filter(l -> {
                    Room r = roomById.get(l.getRoomId());
                    if (r == null) return false;
                    boolean areaOk = areaNeedle == null || areaNeedle.isEmpty()
                            || r.getArea().toLowerCase(Locale.ROOT).contains(areaNeedle);
                    boolean typeOk = type == null || r.getType() == type;
                    return areaOk && typeOk;
                })
                .sorted(Comparator.comparing(Listing::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Listing get(UUID listingId) {
        return listings.findById(listingId).orElseThrow(() -> new NotFoundException("Listing not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Listing getPublic(UUID listingId) {
        Listing listing = get(listingId);
        if (listing.getStatus() != Enums.ListingStatus.LIVE) {
            throw new NotFoundException("Listing not found");
        }
        return listing;
    }

    @Override
    @Transactional(readOnly = true)
    public Room room(UUID roomId) {
        return rooms.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Listing> forLandlord(Actor landlord) {
        LandlordProfile profile = lookup.landlord(landlord);
        List<Listing> result = new ArrayList<>();
        for (Room r : rooms.findByLandlordId(profile.getId())) {
            result.addAll(listings.findByRoomId(r.getId()));
        }
        return result;
    }

    @Override
    @Transactional
    public SavedListing save(Actor tenant, UUID listingId) {
        TenantProfile profile = lookup.tenant(tenant);
        get(listingId);
        return savedListings.findByTenantId(profile.getId()).stream()
                .filter(s -> s.getListingId().equals(listingId))
                .findFirst()
                .orElseGet(() -> savedListings.save(viewingFactory.save(profile.getId(), listingId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedListing> saved(Actor tenant) {
        return savedListings.findByTenantId(lookup.tenant(tenant).getId());
    }

    @Override
    @Transactional
    public Viewing bookViewing(Actor tenant, UUID listingId, Instant at) {
        TenantProfile profile = lookup.tenant(tenant);
        Listing listing = get(listingId);
        if (listing.getStatus() != Enums.ListingStatus.LIVE) {
            throw new ValidationException("This room is not open for viewings");
        }
        Viewing viewing = viewings.save(viewingFactory.book(listingId, profile.getId(), at));
        Room room = room(listing.getRoomId());
        notifications.notify(lookup.userIdOfLandlord(room.getLandlordId()),
                Enums.NotificationType.VIEWING_CONFIRMED, viewing.getId().toString());
        return viewing;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Viewing> viewingsForListing(Actor landlord, UUID listingId) {
        ownedListing(landlord, listingId);
        return viewings.findByListingId(listingId);
    }

    private Listing ownedListing(Actor landlord, UUID listingId) {
        Listing listing = get(listingId);
        lookup.requireLandlordOf(landlord, room(listing.getRoomId()));
        return listing;
    }
}
