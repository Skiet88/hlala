package com.hlala.service;

import com.hlala.domain.DepositHold;
import com.hlala.domain.Enums;
import com.hlala.domain.Lease;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Facade over the Apply -> Pay -> Move in sequence. It spans applications,
 * leases, escrow, listings and notifications, so the API layer calls one
 * method instead of orchestrating five services itself.
 */
public interface TenancyOnboardingFacade {

    record StartedTenancy(Lease lease, DepositHold hold) {
    }

    /** From an accepted application: creates the lease and opens a PENDING deposit hold. */
    StartedTenancy startTenancy(Actor tenant, UUID applicationId, LocalDate startDate, Enums.LeaseLanguage language);

    /** Tenant confirms keys received: hold HELD -> ACTIVE and the listing is marked let. */
    DepositHold confirmMoveIn(Actor tenant, UUID holdId);
}
