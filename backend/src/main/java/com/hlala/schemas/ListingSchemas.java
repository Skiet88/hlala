package com.hlala.schemas;

import com.hlala.domain.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class ListingSchemas {

    private ListingSchemas() {
    }

    public record PinDto(double latitude, double longitude) {
        public GeoPoint toGeoPoint() {
            return new GeoPoint(latitude, longitude);
        }

        public static PinDto of(GeoPoint p) {
            return p == null ? null : new PinDto(p.getLatitude(), p.getLongitude());
        }
    }

    public record CreateRoomRequest(@NotBlank String area, String landmarkNote, PinDto pin,
                                    @NotNull Enums.RoomType type, Set<Enums.Amenity> amenities,
                                    @NotNull Enums.OccupancyMode occupancyMode) {
    }

    public record CreateListingRequest(@NotNull @DecimalMin("1") BigDecimal rentAmount,
                                       @NotNull @DecimalMin("1") BigDecimal depositAmount) {
    }

    public record PublishListingRequest(boolean requestCheck) {
    }

    public record BookViewingRequest(@NotNull @Future Instant scheduledFor) {
    }

    /**
     * The exact pin is deliberately NOT in the public listing views - only the
     * area and landmark. The pin goes to a tenant once a viewing is booked,
     * so a scammer browsing listings cannot map where people live.
     */
    public record RoomDto(UUID id, String area, String landmarkNote, Enums.RoomType type,
                          Set<Enums.Amenity> amenities, Enums.OccupancyMode occupancyMode) {
        public static RoomDto of(Room r) {
            return new RoomDto(r.getId(), r.getArea(), r.getLandmarkNote(), r.getType(),
                    r.getAmenities(), r.getOccupancyMode());
        }
    }

    /** Landlord's own view of their room, including the pin. */
    public record OwnRoomDto(UUID id, String area, String landmarkNote, PinDto pin, Enums.RoomType type,
                             Set<Enums.Amenity> amenities, Enums.OccupancyMode occupancyMode) {
        public static OwnRoomDto of(Room r) {
            return new OwnRoomDto(r.getId(), r.getArea(), r.getLandmarkNote(), PinDto.of(r.getPin()),
                    r.getType(), r.getAmenities(), r.getOccupancyMode());
        }
    }

    public record ListingSummaryDto(UUID id, UUID roomId, BigDecimal rentAmount, Enums.ListingStatus status,
                                    boolean seenInPerson) {
        public static ListingSummaryDto of(Listing l) {
            return new ListingSummaryDto(l.getId(), l.getRoomId(), l.getRentAmount(), l.getStatus(),
                    l.getVerificationStatus() == Enums.VerificationStatus.VERIFIED);
        }
    }

    public record ListingDetailDto(UUID id, BigDecimal rentAmount, BigDecimal depositAmount,
                                   Enums.ListingStatus status, Enums.VerificationStatus verificationStatus,
                                   Instant publishedAt, RoomDto room) {
        public static ListingDetailDto of(Listing l, Room r) {
            return new ListingDetailDto(l.getId(), l.getRentAmount(), l.getDepositAmount(), l.getStatus(),
                    l.getVerificationStatus(), l.getPublishedAt(), RoomDto.of(r));
        }
    }

    public record ViewingDto(UUID id, UUID listingId, Instant scheduledFor, Enums.ViewingStatus status) {
        public static ViewingDto of(Viewing v) {
            return new ViewingDto(v.getId(), v.getListingId(), v.getScheduledFor(), v.getStatus());
        }
    }

    /** What the tenant gets back after booking: now they may see the exact pin. */
    public record BookedViewingDto(ViewingDto viewing, PinDto pin, String landmarkNote) {
    }

    public record SavedListingDto(UUID id, UUID listingId, Instant savedAt) {
        public static SavedListingDto of(SavedListing s) {
            return new SavedListingDto(s.getId(), s.getListingId(), s.getSavedAt());
        }
    }
}
