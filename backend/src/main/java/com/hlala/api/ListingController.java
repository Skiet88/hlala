package com.hlala.api;

import com.hlala.config.CurrentActor;
import com.hlala.domain.Enums;
import com.hlala.domain.Listing;
import com.hlala.domain.Room;
import com.hlala.domain.Viewing;
import com.hlala.schemas.ListingSchemas.*;
import com.hlala.schemas.TenancySchemas.ApplicationDto;
import com.hlala.service.Actor;
import com.hlala.service.ApplicationService;
import com.hlala.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Browsing and acting on listings - the tenant journey up to applying. */
@RestController
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listings;
    private final ApplicationService applications;

    public ListingController(ListingService listings, ApplicationService applications) {
        this.listings = listings;
        this.applications = applications;
    }

    /** Public search - powers the Explore tiles and Filters screen. */
    @GetMapping
    public List<ListingSummaryDto> search(@RequestParam(required = false) String area,
                                          @RequestParam(required = false) BigDecimal maxRent,
                                          @RequestParam(required = false) Enums.RoomType type,
                                          @RequestParam(defaultValue = "false") boolean verifiedOnly) {
        return listings.search(area, maxRent, type, verifiedOnly).stream().map(ListingSummaryDto::of).toList();
    }

    @GetMapping("/{id}")
    public ListingDetailDto get(@PathVariable UUID id) {
        Listing listing = listings.getPublic(id);
        return ListingDetailDto.of(listing, listings.room(listing.getRoomId()));
    }

    @PostMapping("/{id}/viewings")
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public BookedViewingDto bookViewing(@CurrentActor Actor actor, @PathVariable UUID id,
                                        @Valid @RequestBody BookViewingRequest req) {
        Viewing viewing = listings.bookViewing(actor, id, req.scheduledFor());
        Room room = listings.room(listings.get(id).getRoomId());
        return new BookedViewingDto(ViewingDto.of(viewing), PinDto.of(room.getPin()), room.getLandmarkNote());
    }

    @GetMapping("/{id}/viewings")
    @PreAuthorize("hasRole('LANDLORD')")
    public List<ViewingDto> viewings(@CurrentActor Actor actor, @PathVariable UUID id) {
        return listings.viewingsForListing(actor, id).stream().map(ViewingDto::of).toList();
    }

    @PostMapping("/{id}/save")
    @PreAuthorize("hasRole('TENANT')")
    public SavedListingDto save(@CurrentActor Actor actor, @PathVariable UUID id) {
        return SavedListingDto.of(listings.save(actor, id));
    }

    @PostMapping("/{id}/applications")
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDto apply(@CurrentActor Actor actor, @PathVariable UUID id) {
        return ApplicationDto.of(applications.apply(actor, id));
    }

    @GetMapping("/{id}/applicants")
    @PreAuthorize("hasRole('LANDLORD')")
    public List<ApplicationDto> applicants(@CurrentActor Actor actor, @PathVariable UUID id) {
        return applications.forListing(actor, id).stream().map(ApplicationDto::of).toList();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('LANDLORD')")
    public ListingSummaryDto publish(@CurrentActor Actor actor, @PathVariable UUID id,
                                     @RequestBody PublishListingRequest req) {
        return ListingSummaryDto.of(listings.publish(actor, id, req.requestCheck()));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('LANDLORD')")
    public ListingSummaryDto archive(@CurrentActor Actor actor, @PathVariable UUID id) {
        return ListingSummaryDto.of(listings.archive(actor, id));
    }
}
