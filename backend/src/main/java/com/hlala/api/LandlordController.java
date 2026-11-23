package com.hlala.api;

import com.hlala.config.CurrentActor;
import com.hlala.domain.Room;
import com.hlala.schemas.ListingSchemas.*;
import com.hlala.schemas.TenancySchemas.LeaseDto;
import com.hlala.service.Actor;
import com.hlala.service.LeaseService;
import com.hlala.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** The landlord listing flow (pin -> room -> publish) and landlord dashboards. */
@RestController
@PreAuthorize("hasRole('LANDLORD')")
public class LandlordController {

    private final ListingService listings;
    private final LeaseService leases;

    public LandlordController(ListingService listings, LeaseService leases) {
        this.listings = listings;
        this.leases = leases;
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public OwnRoomDto createRoom(@CurrentActor Actor actor, @Valid @RequestBody CreateRoomRequest req) {
        Room room = listings.createRoom(actor, req.area(), req.landmarkNote(),
                req.pin() == null ? null : req.pin().toGeoPoint(), req.type(), req.amenities(), req.occupancyMode());
        return OwnRoomDto.of(room);
    }

    @PostMapping("/rooms/{roomId}/listings")
    @ResponseStatus(HttpStatus.CREATED)
    public ListingSummaryDto createListing(@CurrentActor Actor actor, @PathVariable UUID roomId,
                                           @Valid @RequestBody CreateListingRequest req) {
        return ListingSummaryDto.of(listings.createListing(actor, roomId, req.rentAmount(), req.depositAmount()));
    }

    @GetMapping("/landlords/me/listings")
    public List<ListingSummaryDto> myListings(@CurrentActor Actor actor) {
        return listings.forLandlord(actor).stream().map(ListingSummaryDto::of).toList();
    }

    @GetMapping("/landlords/me/leases")
    public List<LeaseDto> myLeases(@CurrentActor Actor actor) {
        return leases.landlordLeases(actor).stream().map(LeaseDto::of).toList();
    }
}
