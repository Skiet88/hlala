package com.hlala.strategy;

import com.hlala.domain.DepositHold;
import com.hlala.domain.Enums.DepositHoldState;
import com.hlala.domain.Enums.DepositHoldTrigger;
import com.hlala.exception.ApprovalNotSatisfiedException;
import com.hlala.exception.IllegalTransitionException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.hlala.domain.Enums.DepositHoldState.*;
import static com.hlala.domain.Enums.DepositHoldTrigger.*;

/**
 * The full, explicit list of legal DepositHold moves. If an edge is not in
 * this table it cannot happen - there is no other code path that changes a
 * hold's state.
 *
 * Every exit that pays the LANDLORD or splits the money (RELEASED,
 * PARTIALLY_SETTLED) and every refund that comes out of a dispute carries two
 * gates: no open dispute, and the payout approval rule. The one refund
 * without admin approval is HELD -> REFUNDED before move-in, where the money
 * simply goes back to the person who paid it.
 */
@Component
public class DepositHoldTransitionTable {

    private final List<DepositHoldTransition> transitions;

    public DepositHoldTransitionTable(PayoutApprovalStrategy approvalStrategy) {
        DepositHoldTransition.TransitionGuard none = (hold, ctx) -> {
        };

        DepositHoldTransition.TransitionGuard noOpenDispute = (hold, ctx) -> {
            if (ctx.openDispute()) {
                throw new IllegalTransitionException(
                        "Hold " + hold.getId() + " has an open dispute, so no money can move");
            }
        };

        DepositHoldTransition.TransitionGuard approved = (hold, ctx) -> {
            if (!approvalStrategy.isSatisfied(ctx.approvals())) {
                throw new ApprovalNotSatisfiedException(approvalStrategy.describeRule());
            }
        };

        DepositHoldTransition.TransitionGuard handoverConfirmed = (hold, ctx) -> {
            if (!ctx.handoverConfirmed()) {
                throw new IllegalTransitionException(
                        "The tenant has not confirmed the key handover for hold " + hold.getId());
            }
        };

        DepositHoldTransition.TransitionGuard payoutGate = all(noOpenDispute, approved);

        this.transitions = List.of(
                new DepositHoldTransition(PENDING, PAYMENT_CONFIRMED, HELD, none),
                new DepositHoldTransition(HELD, MOVE_IN_CONFIRMED, ACTIVE, none),
                new DepositHoldTransition(HELD, CANCELLED_BEFORE_MOVE_IN, REFUNDED, none),
                new DepositHoldTransition(ACTIVE, DISPUTE_OPENED, DISPUTED, none),
                new DepositHoldTransition(ACTIVE, RELEASE_AFTER_HANDOVER, RELEASED, all(handoverConfirmed, payoutGate)),
                new DepositHoldTransition(DISPUTED, RESOLVED_FOR_LANDLORD, RELEASED, payoutGate),
                new DepositHoldTransition(DISPUTED, RESOLVED_FOR_TENANT, REFUNDED, payoutGate),
                new DepositHoldTransition(DISPUTED, RESOLVED_SPLIT, PARTIALLY_SETTLED, payoutGate)
        );
    }

    public Optional<DepositHoldTransition> find(DepositHoldState from, DepositHoldTrigger trigger) {
        return transitions.stream()
                .filter(t -> t.from() == from && t.trigger() == trigger)
                .findFirst();
    }

    /** Looks up the edge and runs its guards. Throws if there is no edge or a guard fails. */
    public DepositHoldTransition resolve(DepositHold hold, DepositHoldTrigger trigger, TransitionContext ctx) {
        DepositHoldTransition transition = find(hold.getState(), trigger)
                .orElseThrow(() -> new IllegalTransitionException(
                        "Cannot apply " + trigger + " to a hold in state " + hold.getState()));
        transition.checkGuard(hold, ctx);
        return transition;
    }

    public List<DepositHoldTransition> all() {
        return transitions;
    }

    private static DepositHoldTransition.TransitionGuard all(DepositHoldTransition.TransitionGuard... guards) {
        return (hold, ctx) -> {
            for (DepositHoldTransition.TransitionGuard g : guards) {
                g.check(hold, ctx);
            }
        };
    }
}
