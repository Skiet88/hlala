package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.repository.*;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * One place for "who is this user in this role" and "is this user a party to
 * that thing". Every ownership check in the service layer goes through here,
 * so the rule is written once rather than re-implemented per service.
 *
 * Ownership lives in services, not only in controller annotations, so it
 * still holds if a future endpoint forgets its @PreAuthorize.
 */
@Component
public class ProfileLookup {

    private final TenantProfileRepository tenants;
    private final LandlordProfileRepository landlords;
    private final CheckerProfileRepository checkers;
    private final AdminProfileRepository admins;

    public ProfileLookup(TenantProfileRepository tenants, LandlordProfileRepository landlords,
                         CheckerProfileRepository checkers, AdminProfileRepository admins) {
        this.tenants = tenants;
        this.landlords = landlords;
        this.checkers = checkers;
        this.admins = admins;
    }

    public TenantProfile tenant(Actor actor) {
        return tenants.findByUserId(actor.userId())
                .orElseThrow(() -> new ForbiddenOperationException("You need a tenant profile for this"));
    }

    public LandlordProfile landlord(Actor actor) {
        return landlords.findByUserId(actor.userId())
                .orElseThrow(() -> new ForbiddenOperationException("You need a landlord profile for this"));
    }

    public CheckerProfile checker(Actor actor) {
        return checkers.findByUserId(actor.userId())
                .orElseThrow(() -> new ForbiddenOperationException("You need a checker profile for this"));
    }

    public AdminProfile admin(Actor actor) {
        return admins.findByUserId(actor.userId())
                .orElseThrow(() -> new ForbiddenOperationException("You need an admin profile for this"));
    }

    public UUID userIdOfTenant(UUID tenantProfileId) {
        return tenants.findById(tenantProfileId).map(TenantProfile::getUserId)
                .orElseThrow(() -> new NotFoundException("Tenant profile not found"));
    }

    public UUID userIdOfLandlord(UUID landlordProfileId) {
        return landlords.findById(landlordProfileId).map(LandlordProfile::getUserId)
                .orElseThrow(() -> new NotFoundException("Landlord profile not found"));
    }

    public boolean isTenantOf(Actor actor, Lease lease) {
        Optional<TenantProfile> p = tenants.findByUserId(actor.userId());
        return p.isPresent() && p.get().getId().equals(lease.getTenantId());
    }

    public boolean isLandlordOf(Actor actor, Lease lease) {
        Optional<LandlordProfile> p = landlords.findByUserId(actor.userId());
        return p.isPresent() && p.get().getId().equals(lease.getLandlordId());
    }

    public boolean isLandlordOf(Actor actor, Room room) {
        Optional<LandlordProfile> p = landlords.findByUserId(actor.userId());
        return p.isPresent() && p.get().getId().equals(room.getLandlordId());
    }

    public boolean isPartyTo(Actor actor, Lease lease) {
        return isTenantOf(actor, lease) || isLandlordOf(actor, lease);
    }

    public void requirePartyTo(Actor actor, Lease lease) {
        if (!isPartyTo(actor, lease)) {
            throw new ForbiddenOperationException("You are not a party to this lease");
        }
    }

    public void requireLandlordOf(Actor actor, Room room) {
        if (!isLandlordOf(actor, room)) {
            throw new ForbiddenOperationException("This is not your room");
        }
    }

    /** Roles are derived from which profiles exist - there is no separate, drift-prone role column. */
    public Set<Enums.Role> rolesOf(UUID userId) {
        Set<Enums.Role> roles = EnumSet.noneOf(Enums.Role.class);
        if (tenants.findByUserId(userId).isPresent()) roles.add(Enums.Role.TENANT);
        if (landlords.findByUserId(userId).isPresent()) roles.add(Enums.Role.LANDLORD);
        if (checkers.findByUserId(userId).isPresent()) roles.add(Enums.Role.CHECKER);
        if (admins.findByUserId(userId).isPresent()) roles.add(Enums.Role.ADMIN);
        return roles;
    }
}
