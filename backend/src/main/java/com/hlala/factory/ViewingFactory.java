package com.hlala.factory;

import com.hlala.domain.Enums;
import com.hlala.domain.SavedListing;
import com.hlala.domain.Viewing;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/** Discovery-side records: booked viewings and saved listings. */
@Component
public class ViewingFactory {

    public Viewing book(UUID listingId, UUID tenantId, Instant scheduledFor) {
        if (listingId == null || tenantId == null) {
            throw new ValidationException("A viewing needs a listing and a tenant");
        }
        if (scheduledFor == null || !scheduledFor.isAfter(Instant.now())) {
            throw new ValidationException("A viewing must be booked for a time in the future");
        }
        return new Viewing(UUID.randomUUID(), listingId, tenantId, scheduledFor, Enums.ViewingStatus.BOOKED);
    }

    public SavedListing save(UUID tenantId, UUID listingId) {
        if (listingId == null || tenantId == null) {
            throw new ValidationException("Saving needs a listing and a tenant");
        }
        return new SavedListing(UUID.randomUUID(), tenantId, listingId, Instant.now());
    }
}
