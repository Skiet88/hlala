package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.DepositHoldFactory;
import com.hlala.factory.LeaseFactory;
import com.hlala.repository.DepositHoldRepository;
import com.hlala.repository.LeaseRepository;
import com.hlala.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
public class TenancyOnboardingFacadeImpl implements TenancyOnboardingFacade {

    private static final Set<Enums.DepositHoldState> LIVE_HOLD_STATES =
            EnumSet.of(Enums.DepositHoldState.PENDING, Enums.DepositHoldState.HELD,
                    Enums.DepositHoldState.ACTIVE, Enums.DepositHoldState.DISPUTED);

    private final ApplicationService applicationService;
    private final ListingService listingService;
    private final EscrowService escrowService;
    private final NotificationService notifications;
    private final ProfileLookup lookup;
    private final LeaseRepository leases;
    private final DepositHoldRepository holds;
    private final ListingRepository listings;
    private final LeaseFactory leaseFactory;
    private final DepositHoldFactory holdFactory;

    public TenancyOnboardingFacadeImpl(ApplicationService applicationService, ListingService listingService,
                                       EscrowService escrowService, NotificationService notifications,
                                       ProfileLookup lookup, LeaseRepository leases, DepositHoldRepository holds,
                                       ListingRepository listings, LeaseFactory leaseFactory,
                                       DepositHoldFactory holdFactory) {
        this.applicationService = applicationService;
        this.listingService = listingService;
        this.escrowService = escrowService;
        this.notifications = notifications;
        this.lookup = lookup;
        this.leases = leases;
        this.holds = holds;
        this.listings = listings;
        this.leaseFactory = leaseFactory;
        this.holdFactory = holdFactory;
    }

    @Override
    @Transactional
    public StartedTenancy startTenancy(Actor tenant, UUID applicationId, LocalDate startDate,
                                       Enums.LeaseLanguage language) {
        TenantProfile tenantProfile = lookup.tenant(tenant);
        Application application = applicationService.get(applicationId);
        if (!application.getTenantId().equals(tenantProfile.getId())) {
            throw new ForbiddenOperationException("This is not your application");
        }
        if (application.getStatus() != Enums.ApplicationStatus.ACCEPTED) {
            throw new ValidationException("The landlord has not accepted this application yet");
        }

        Listing listing = listingService.get(application.getListingId());
        if (listing.getStatus() != Enums.ListingStatus.LIVE) {
            throw new ValidationException("This room is no longer available");
        }
        Room room = listingService.room(listing.getRoomId());

        boolean alreadyStarted = leases.findByRoomId(room.getId()).stream()
                .filter(l -> l.getTenantId().equals(tenantProfile.getId()))
                .anyMatch(l -> holds.findByLeaseId(l.getId())
                        .map(h -> LIVE_HOLD_STATES.contains(h.getState())).orElse(false));
        if (alreadyStarted) {
            throw new ValidationException("You already have a tenancy in progress for this room");
        }

        Lease lease = leases.save(leaseFactory.create(room.getId(), tenantProfile.getId(), room.getLandlordId(),
                listing.getRentAmount(), startDate, null, language));
        DepositHold hold = holds.save(holdFactory.open(lease.getId(), listing.getDepositAmount()));
        escrowService.recordOpened(hold, tenant.userId());

        notifications.notify(lookup.userIdOfLandlord(room.getLandlordId()),
                Enums.NotificationType.GENERAL, lease.getId().toString());
        return new StartedTenancy(lease, hold);
    }

    @Override
    @Transactional
    public DepositHold confirmMoveIn(Actor tenant, UUID holdId) {
        DepositHold hold = escrowService.get(holdId);
        Lease lease = leases.findById(hold.getLeaseId())
                .orElseThrow(() -> new NotFoundException("Lease not found"));
        if (!lookup.isTenantOf(tenant, lease)) {
            throw new ForbiddenOperationException("Only the tenant can confirm move-in");
        }

        DepositHold active = escrowService.transition(holdId, Enums.DepositHoldTrigger.MOVE_IN_CONFIRMED,
                tenant.userId(), "Tenant confirmed keys received");

        Room room = listingService.room(lease.getRoomId());
        if (room.getOccupancyMode() == Enums.OccupancyMode.SOLE) {
            listings.findByRoomId(room.getId()).stream()
                    .filter(l -> l.getStatus() == Enums.ListingStatus.LIVE)
                    .forEach(l -> listings.save(l.let()));
        }
        return active;
    }
}
