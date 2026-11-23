package com.hlala.service;

import com.hlala.domain.Enums;
import com.hlala.domain.RepairTicket;
import com.hlala.domain.Review;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Day-to-day life in the room and leaving well: repairs and sealed reviews. */
public interface LivingService {

    RepairTicket reportRepair(Actor tenant, UUID leaseId, String description);

    /**
     * Moves a repair one step forward only. Landlord: REPORTED -> SEEN -> BOOKED.
     * Tenant: BOOKED -> CONFIRMED_FIXED. A landlord cannot mark their own repair as fixed.
     */
    RepairTicket advanceRepair(Actor actor, UUID ticketId, Enums.RepairStatus next);

    List<RepairTicket> repairs(Actor party, UUID leaseId);

    Review postReview(Actor party, UUID leaseId, int stars, Set<String> tags, String comment);

    /** Sealed reviews written by the other side are hidden until both sides have posted. */
    List<Review> reviews(Actor party, UUID leaseId);
}
