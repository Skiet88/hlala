package com.hlala.strategy;

import com.hlala.domain.DepositHold;
import com.hlala.domain.Enums.DepositHoldState;
import com.hlala.domain.Enums.DepositHoldTrigger;

/**
 * One legal edge in the DepositHold state machine. The guard throws a domain
 * exception when the move is not allowed - it never returns false silently,
 * because a silent no-op on money is exactly the bug this design exists to stop.
 */
public record DepositHoldTransition(
        DepositHoldState from,
        DepositHoldTrigger trigger,
        DepositHoldState to,
        TransitionGuard guard
) {

    @FunctionalInterface
    public interface TransitionGuard {
        void check(DepositHold hold, TransitionContext context);
    }

    public void checkGuard(DepositHold hold, TransitionContext context) {
        guard.check(hold, context);
    }
}
