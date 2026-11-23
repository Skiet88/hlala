package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ValidationException;
import com.hlala.factory.RoomCheckFactory;
import com.hlala.repository.ListingRepository;
import com.hlala.repository.RoomCheckRepository;
import com.hlala.screening.RoomCheckScreening;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RoomCheckServiceImpl implements RoomCheckService {

    private final RoomCheckRepository checks;
    private final ListingRepository listings;
    private final ListingService listingService;
    private final RoomCheckFactory factory;
    private final RoomCheckScreening screening;
    private final ProfileLookup lookup;
    private final NotificationService notifications;

    public RoomCheckServiceImpl(RoomCheckRepository checks, ListingRepository listings,
                                ListingService listingService, RoomCheckFactory factory,
                                RoomCheckScreening screening, ProfileLookup lookup,
                                NotificationService notifications) {
        this.checks = checks;
        this.listings = listings;
        this.listingService = listingService;
        this.factory = factory;
        this.screening = screening;
        this.lookup = lookup;
        this.notifications = notifications;
    }

    /**
     * The badge is earned only if every checker answer AND every screening rule
     * passes. Screening lines are stored with the check, so a failed visit
     * shows exactly why (e.g. "Visit was 2300 m from the pinned room").
     */
    @Override
    @Transactional
    public RoomCheck submit(Actor checker, UUID listingId, List<ChecklistItemResult> results, List<URI> photos,
                            GeoPoint checkedAt) {
        CheckerProfile profile = lookup.checker(checker);
        Listing listing = listingService.get(listingId);
        if (listing.getVerificationStatus() != Enums.VerificationStatus.PENDING_CHECK) {
            throw new ValidationException("This listing is not waiting for a check");
        }
        if (results == null || results.isEmpty()) {
            throw new ValidationException("A room check must include checklist results");
        }
        Room room = listingService.room(listing.getRoomId());

        List<ChecklistItemResult> all = new ArrayList<>(results);
        all.addAll(screening.screen(room, results, photos, checkedAt));
        RoomCheck check = checks.save(factory.create(listingId, profile.getId(), all, photos, checkedAt));

        listings.save(listing.withVerificationStatus(
                check.isPassed() ? Enums.VerificationStatus.VERIFIED : Enums.VerificationStatus.UNVERIFIED));
        notifications.notify(lookup.userIdOfLandlord(room.getLandlordId()),
                Enums.NotificationType.GENERAL, check.getId().toString());
        return check;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Listing> queue(Actor checker) {
        lookup.checker(checker);
        return listings.findByVerificationStatus(Enums.VerificationStatus.PENDING_CHECK);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCheck> mine(Actor checker) {
        return checks.findByCheckerId(lookup.checker(checker).getId());
    }
}
