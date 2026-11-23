package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.IllegalTransitionException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.EscrowRecordFactory;
import com.hlala.repository.*;
import com.hlala.strategy.DepositHoldTransition;
import com.hlala.strategy.DepositHoldTransitionTable;
import com.hlala.strategy.PayoutApprovalStrategy;
import com.hlala.strategy.TransitionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.hlala.domain.Enums.DepositHoldState.*;

/**
 * Approval scoping: only approvals given AFTER the hold's most recent event
 * count. Any new event (handover confirmed, dispute opened) wipes the slate,
 * so an approval can never be carried over to a decision the admin did not see.
 */
@Service
public class EscrowServiceImpl implements EscrowService {

    private static final Set<Enums.DepositHoldState> TERMINAL = EnumSet.of(RELEASED, REFUNDED, PARTIALLY_SETTLED);

    private final DepositHoldRepository holds;
    private final DepositHoldEventRepository events;
    private final AdminApprovalRepository approvals;
    private final DisputeRepository disputes;
    private final LeaseRepository leases;
    private final DepositHoldTransitionTable table;
    private final PayoutApprovalStrategy approvalStrategy;
    private final EscrowRecordFactory recordFactory;
    private final ProfileLookup lookup;
    private final NotificationService notifications;

    public EscrowServiceImpl(DepositHoldRepository holds, DepositHoldEventRepository events,
                             AdminApprovalRepository approvals, DisputeRepository disputes,
                             LeaseRepository leases, DepositHoldTransitionTable table,
                             PayoutApprovalStrategy approvalStrategy, EscrowRecordFactory recordFactory,
                             ProfileLookup lookup, NotificationService notifications) {
        this.holds = holds;
        this.events = events;
        this.approvals = approvals;
        this.disputes = disputes;
        this.leases = leases;
        this.table = table;
        this.approvalStrategy = approvalStrategy;
        this.recordFactory = recordFactory;
        this.lookup = lookup;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public void recordOpened(DepositHold hold, UUID openedByUserId) {
        events.save(recordFactory.event(hold.getId(), hold.getState(), hold.getState(), openedByUserId,
                "Deposit hold opened"));
    }

    @Override
    @Transactional
    public DepositHold transition(UUID holdId, Enums.DepositHoldTrigger trigger, UUID actorUserId, String reason) {
        DepositHold hold = get(holdId);
        TransitionContext ctx = contextFor(hold, actorUserId, reason);
        DepositHoldTransition edge = table.resolve(hold, trigger, ctx);

        DepositHold next = holds.save(hold.withState(edge.to()));
        events.save(recordFactory.event(hold.getId(), edge.from(), edge.to(), actorUserId,
                reason != null ? reason : trigger.name()));

        notifyParties(next, TERMINAL.contains(next.getState())
                ? Enums.NotificationType.DEPOSIT_RELEASED
                : Enums.NotificationType.GENERAL);
        return next;
    }

    @Override
    @Transactional
    public DepositHold confirmPayment(Actor admin, UUID holdId) {
        lookup.admin(admin);
        return transition(holdId, Enums.DepositHoldTrigger.PAYMENT_CONFIRMED, admin.userId(), "Payment confirmed");
    }

    @Override
    @Transactional
    public DepositHold confirmHandover(Actor tenant, UUID holdId) {
        DepositHold hold = get(holdId);
        Lease lease = leaseOf(hold);
        if (!lookup.isTenantOf(tenant, lease)) {
            throw new ForbiddenOperationException("Only the tenant can confirm the key handover");
        }
        if (hold.getState() != ACTIVE) {
            throw new IllegalTransitionException("Handover can only be confirmed while the hold is active");
        }
        if (hold.getHandoverConfirmedAt() != null) {
            return hold;
        }
        DepositHold updated = holds.save(hold.withHandoverConfirmed(Instant.now()));
        events.save(recordFactory.event(hold.getId(), ACTIVE, ACTIVE, tenant.userId(),
                "Tenant confirmed key handover"));
        notifications.notifyAdmins(Enums.NotificationType.GENERAL, hold.getId().toString());
        return updated;
    }

    @Override
    @Transactional
    public DepositHold cancelBeforeMoveIn(Actor party, UUID holdId) {
        DepositHold hold = get(holdId);
        lookup.requirePartyTo(party, leaseOf(hold));
        return transition(holdId, Enums.DepositHoldTrigger.CANCELLED_BEFORE_MOVE_IN, party.userId(),
                "Cancelled before move-in");
    }

    @Override
    @Transactional
    public AdminApproval approve(Actor admin, UUID holdId) {
        AdminProfile adminProfile = lookup.admin(admin);
        // Locked for the decision: an approval writes a new row but not the hold,
        // so without this two admins approving at once would each see only their
        // own approval and neither would release. With it, the second commit
        // conflicts and the admin retries, now seeing both approvals.
        DepositHold hold = holds.findForDecisionById(holdId)
                .orElseThrow(() -> new NotFoundException("Deposit hold not found"));
        Lease lease = leaseOf(hold);

        if (lookup.isPartyTo(admin, lease)) {
            throw new ForbiddenOperationException("You are a party to this lease, so you cannot approve its payout");
        }
        if (pendingPayoutTrigger(hold).isEmpty()) {
            throw new IllegalTransitionException("There is no payout waiting for approval on this hold");
        }
        boolean alreadyApproved = currentApprovals(holdId).stream()
                .anyMatch(a -> a.getAdminProfileId().equals(adminProfile.getId()));
        if (alreadyApproved) {
            throw new ValidationException("You have already approved this payout; a different admin must approve");
        }

        AdminApproval approval = approvals.save(recordFactory.approval(holdId, adminProfile.getId()));
        tryFinalize(holdId, admin.userId());
        return approval;
    }

    @Override
    @Transactional
    public Optional<DepositHold> tryFinalize(UUID holdId, UUID actorUserId) {
        DepositHold hold = get(holdId);
        Optional<Enums.DepositHoldTrigger> trigger = pendingPayoutTrigger(hold);
        if (trigger.isEmpty() || !approvalStrategy.isSatisfied(currentApprovals(holdId))) {
            return Optional.empty();
        }
        return Optional.of(transition(holdId, trigger.get(), actorUserId, "Payout approved"));
    }

    @Override
    @Transactional(readOnly = true)
    public DepositHold getForParty(Actor actor, UUID holdId) {
        DepositHold hold = get(holdId);
        lookup.requirePartyTo(actor, leaseOf(hold));
        return hold;
    }

    @Override
    @Transactional(readOnly = true)
    public DepositHold getForLease(UUID leaseId) {
        return holds.findByLeaseId(leaseId)
                .orElseThrow(() -> new NotFoundException("No deposit hold for this lease"));
    }

    @Override
    @Transactional(readOnly = true)
    public DepositHold get(UUID holdId) {
        return holds.findById(holdId).orElseThrow(() -> new NotFoundException("Deposit hold not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositHoldEvent> history(UUID holdId) {
        return events.findByDepositHoldIdOrderByOccurredAtAsc(holdId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminApproval> currentApprovals(UUID holdId) {
        Instant since = lastEventAt(holdId);
        return approvals.findByDepositHoldId(holdId).stream()
                .filter(a -> since == null || a.getApprovedAt().isAfter(since))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositHoldEvent> moneyTrail() {
        return events.findTop200ByOrderByOccurredAtDesc();
    }

    /**
     * Which payout is waiting, if any:
     *  - ACTIVE with handover confirmed and no open dispute -> release to landlord
     *  - DISPUTED whose disputes are all resolved -> follow the latest resolution
     */
    private Optional<Enums.DepositHoldTrigger> pendingPayoutTrigger(DepositHold hold) {
        boolean openDispute = !disputes.findByDepositHoldIdAndStatus(hold.getId(), Enums.DisputeStatus.OPEN).isEmpty();
        if (openDispute) {
            return Optional.empty();
        }
        if (hold.getState() == ACTIVE && hold.getHandoverConfirmedAt() != null) {
            return Optional.of(Enums.DepositHoldTrigger.RELEASE_AFTER_HANDOVER);
        }
        if (hold.getState() == DISPUTED) {
            return disputes.findByDepositHoldId(hold.getId()).stream()
                    .filter(d -> d.getStatus() == Enums.DisputeStatus.RESOLVED && d.getResolvedAt() != null)
                    .max(Comparator.comparing(Dispute::getResolvedAt))
                    .map(d -> switch (d.getResolution()) {
                        case LANDLORD -> Enums.DepositHoldTrigger.RESOLVED_FOR_LANDLORD;
                        case TENANT -> Enums.DepositHoldTrigger.RESOLVED_FOR_TENANT;
                        case SPLIT -> Enums.DepositHoldTrigger.RESOLVED_SPLIT;
                    });
        }
        return Optional.empty();
    }

    private TransitionContext contextFor(DepositHold hold, UUID actorUserId, String reason) {
        boolean openDispute = !disputes.findByDepositHoldIdAndStatus(hold.getId(), Enums.DisputeStatus.OPEN).isEmpty();
        return new TransitionContext(actorUserId, reason, hold.getHandoverConfirmedAt() != null,
                openDispute, currentApprovals(hold.getId()));
    }

    private Instant lastEventAt(UUID holdId) {
        List<DepositHoldEvent> all = events.findByDepositHoldIdOrderByOccurredAtAsc(holdId);
        return all.isEmpty() ? null : all.get(all.size() - 1).getOccurredAt();
    }

    private Lease leaseOf(DepositHold hold) {
        return leases.findById(hold.getLeaseId())
                .orElseThrow(() -> new NotFoundException("Lease for this hold not found"));
    }

    private void notifyParties(DepositHold hold, Enums.NotificationType type) {
        Lease lease = leaseOf(hold);
        notifications.notify(lookup.userIdOfTenant(lease.getTenantId()), type, hold.getId().toString());
        notifications.notify(lookup.userIdOfLandlord(lease.getLandlordId()), type, hold.getId().toString());
    }
}
