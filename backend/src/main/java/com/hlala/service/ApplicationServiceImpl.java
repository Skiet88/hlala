package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.ApplicationFactory;
import com.hlala.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applications;
    private final ApplicationFactory factory;
    private final ListingService listingService;
    private final ProfileLookup lookup;
    private final NotificationService notifications;

    public ApplicationServiceImpl(ApplicationRepository applications, ApplicationFactory factory,
                                  ListingService listingService, ProfileLookup lookup,
                                  NotificationService notifications) {
        this.applications = applications;
        this.factory = factory;
        this.listingService = listingService;
        this.lookup = lookup;
        this.notifications = notifications;
    }

    /**
     * Verification ticks start false. They are set by the verification flow
     * (ID check, income proof, references), never self-asserted by the tenant -
     * a tick the tenant can set for themselves would be worthless to a landlord.
     */
    @Override
    @Transactional
    public Application apply(Actor tenant, UUID listingId) {
        TenantProfile profile = lookup.tenant(tenant);
        Listing listing = listingService.get(listingId);
        if (listing.getStatus() != Enums.ListingStatus.LIVE) {
            throw new ValidationException("This room is not taking applications");
        }
        boolean alreadyPending = applications.findByTenantId(profile.getId()).stream()
                .anyMatch(a -> a.getListingId().equals(listingId) && a.getStatus() == Enums.ApplicationStatus.PENDING);
        if (alreadyPending) {
            throw new ValidationException("You already have a pending application for this room");
        }
        Application saved = applications.save(factory.create(listingId, profile.getId(), false, false, false));
        Room room = listingService.room(listing.getRoomId());
        notifications.notify(lookup.userIdOfLandlord(room.getLandlordId()),
                Enums.NotificationType.GENERAL, saved.getId().toString());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> forListing(Actor landlord, UUID listingId) {
        Listing listing = listingService.get(listingId);
        lookup.requireLandlordOf(landlord, listingService.room(listing.getRoomId()));
        return applications.findByListingId(listingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> mine(Actor tenant) {
        return applications.findByTenantId(lookup.tenant(tenant).getId());
    }

    @Override
    @Transactional
    public Application decide(Actor landlord, UUID applicationId, boolean accept) {
        Application application = get(applicationId);
        Listing listing = listingService.get(application.getListingId());
        lookup.requireLandlordOf(landlord, listingService.room(listing.getRoomId()));
        if (application.getStatus() != Enums.ApplicationStatus.PENDING) {
            throw new ValidationException("This application has already been decided");
        }
        Application decided = applications.save(application.withStatus(
                accept ? Enums.ApplicationStatus.ACCEPTED : Enums.ApplicationStatus.DECLINED));
        notifications.notify(lookup.userIdOfTenant(application.getTenantId()),
                Enums.NotificationType.APPLICATION_DECISION, decided.getId().toString());
        return decided;
    }

    @Override
    @Transactional
    public Application withdraw(Actor tenant, UUID applicationId) {
        Application application = get(applicationId);
        if (!lookup.tenant(tenant).getId().equals(application.getTenantId())) {
            throw new ForbiddenOperationException("This is not your application");
        }
        if (application.getStatus() != Enums.ApplicationStatus.PENDING
                && application.getStatus() != Enums.ApplicationStatus.ACCEPTED) {
            throw new ValidationException("This application can no longer be withdrawn");
        }
        return applications.save(application.withStatus(Enums.ApplicationStatus.WITHDRAWN));
    }

    @Override
    @Transactional(readOnly = true)
    public Application get(UUID applicationId) {
        return applications.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
    }
}
