package com.hlala.service;

import com.hlala.domain.AdminApproval;
import com.hlala.domain.DepositHold;
import com.hlala.domain.DepositHoldEvent;
import com.hlala.domain.Enums;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The only component allowed to change a DepositHold's state.
 */
public interface EscrowService {

    /** Writes the opening audit row for a newly created hold. */
    void recordOpened(DepositHold hold, UUID openedByUserId);

    /** The single entry point for every state change. Throws on an illegal or unguarded move. */
    DepositHold transition(UUID holdId, Enums.DepositHoldTrigger trigger, UUID actorUserId, String reason);

    /** Stand-in for a payment-provider webhook until one is integrated. Admin only. */
    DepositHold confirmPayment(Actor admin, UUID holdId);

    /** Tenant confirms keys handed back at move-out. Does not move money on its own. */
    DepositHold confirmHandover(Actor tenant, UUID holdId);

    /** Either party cancels before move-in; money goes back to the tenant. */
    DepositHold cancelBeforeMoveIn(Actor party, UUID holdId);

    /**
     * Records one admin's approval, then releases the money if the payout rule
     * is now met. Rejects approvals from an admin who is a party to the lease,
     * duplicate approvals, and approvals when there is nothing to approve yet.
     */
    AdminApproval approve(Actor admin, UUID holdId);

    /** If everything needed for a payout is in place, performs it. Otherwise does nothing. */
    Optional<DepositHold> tryFinalize(UUID holdId, UUID actorUserId);

    DepositHold getForParty(Actor actor, UUID holdId);

    DepositHold getForLease(UUID leaseId);

    DepositHold get(UUID holdId);

    List<DepositHoldEvent> history(UUID holdId);

    List<AdminApproval> currentApprovals(UUID holdId);

    List<DepositHoldEvent> moneyTrail();
}
