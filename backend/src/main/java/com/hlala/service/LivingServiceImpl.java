package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.CareFactory;
import com.hlala.repository.DepositHoldRepository;
import com.hlala.repository.LeaseRepository;
import com.hlala.repository.RepairTicketRepository;
import com.hlala.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LivingServiceImpl implements LivingService {

    private static final Set<Enums.DepositHoldState> OCCUPIED =
            EnumSet.of(Enums.DepositHoldState.ACTIVE, Enums.DepositHoldState.DISPUTED);
    private static final Set<Enums.DepositHoldState> ENDED =
            EnumSet.of(Enums.DepositHoldState.RELEASED, Enums.DepositHoldState.REFUNDED,
                    Enums.DepositHoldState.PARTIALLY_SETTLED);

    private final RepairTicketRepository repairs;
    private final ReviewRepository reviews;
    private final LeaseRepository leases;
    private final DepositHoldRepository holds;
    private final CareFactory factory;
    private final ProfileLookup lookup;
    private final NotificationService notifications;

    public LivingServiceImpl(RepairTicketRepository repairs, ReviewRepository reviews, LeaseRepository leases,
                             DepositHoldRepository holds, CareFactory factory, ProfileLookup lookup,
                             NotificationService notifications) {
        this.repairs = repairs;
        this.reviews = reviews;
        this.leases = leases;
        this.holds = holds;
        this.factory = factory;
        this.lookup = lookup;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public RepairTicket reportRepair(Actor tenant, UUID leaseId, String description) {
        Lease lease = lease(leaseId);
        if (!lookup.isTenantOf(tenant, lease)) {
            throw new ForbiddenOperationException("Only the tenant can report a repair");
        }
        if (!OCCUPIED.contains(holdState(leaseId))) {
            throw new ValidationException("Repairs can be reported while you are living there");
        }
        RepairTicket ticket = repairs.save(factory.repair(leaseId, description));
        notifications.notify(lookup.userIdOfLandlord(lease.getLandlordId()),
                Enums.NotificationType.REPAIR_UPDATE, ticket.getId().toString());
        return ticket;
    }

    @Override
    @Transactional
    public RepairTicket advanceRepair(Actor actor, UUID ticketId, Enums.RepairStatus next) {
        RepairTicket ticket = repairs.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Repair not found"));
        Lease lease = lease(ticket.getLeaseId());
        if (next == null || next.ordinal() != ticket.getStatus().ordinal() + 1) {
            throw new ValidationException("A repair moves one step at a time; it is currently " + ticket.getStatus());
        }
        boolean tenantStep = next == Enums.RepairStatus.CONFIRMED_FIXED;
        if (tenantStep && !lookup.isTenantOf(actor, lease)) {
            throw new ForbiddenOperationException("Only the tenant can confirm a repair is fixed");
        }
        if (!tenantStep && !lookup.isLandlordOf(actor, lease)) {
            throw new ForbiddenOperationException("Only the landlord can update this repair");
        }
        RepairTicket updated = repairs.save(ticket.withStatus(next));
        UUID other = tenantStep ? lookup.userIdOfLandlord(lease.getLandlordId())
                : lookup.userIdOfTenant(lease.getTenantId());
        notifications.notify(other, Enums.NotificationType.REPAIR_UPDATE, updated.getId().toString());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairTicket> repairs(Actor party, UUID leaseId) {
        lookup.requirePartyTo(party, lease(leaseId));
        return repairs.findByLeaseId(leaseId);
    }

    @Override
    @Transactional
    public Review postReview(Actor party, UUID leaseId, int stars, Set<String> tags, String comment) {
        Lease lease = lease(leaseId);
        UUID tenantUser = lookup.userIdOfTenant(lease.getTenantId());
        UUID landlordUser = lookup.userIdOfLandlord(lease.getLandlordId());
        UUID about;
        if (lookup.isTenantOf(party, lease)) {
            about = landlordUser;
        } else if (lookup.isLandlordOf(party, lease)) {
            about = tenantUser;
        } else {
            throw new ForbiddenOperationException("You are not a party to this lease");
        }
        if (!ENDED.contains(holdState(leaseId))) {
            throw new ValidationException("Reviews open once the tenancy has ended and the deposit is settled");
        }
        List<Review> existing = reviews.findByLeaseId(leaseId);
        if (existing.stream().anyMatch(r -> r.getAuthorId().equals(party.userId()))) {
            throw new ValidationException("You have already reviewed this tenancy");
        }

        Review mine = reviews.save(factory.review(leaseId, party.userId(), about, stars, tags, comment));

        // Both sides in: unseal together, so neither side wrote in reaction to the other.
        if (!existing.isEmpty()) {
            for (Review r : reviews.findByLeaseId(leaseId)) {
                reviews.save(r.unsealed());
            }
            notifications.notify(tenantUser, Enums.NotificationType.REVIEW_UNLOCKED, leaseId.toString());
            notifications.notify(landlordUser, Enums.NotificationType.REVIEW_UNLOCKED, leaseId.toString());
            return mine.unsealed();
        }
        return mine;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> reviews(Actor party, UUID leaseId) {
        lookup.requirePartyTo(party, lease(leaseId));
        return reviews.findByLeaseId(leaseId).stream()
                .filter(r -> !r.isSealed() || r.getAuthorId().equals(party.userId()))
                .toList();
    }

    private Lease lease(UUID leaseId) {
        return leases.findById(leaseId).orElseThrow(() -> new NotFoundException("Lease not found"));
    }

    private Enums.DepositHoldState holdState(UUID leaseId) {
        return holds.findByLeaseId(leaseId).map(DepositHold::getState)
                .orElseThrow(() -> new NotFoundException("No deposit hold for this lease"));
    }
}
