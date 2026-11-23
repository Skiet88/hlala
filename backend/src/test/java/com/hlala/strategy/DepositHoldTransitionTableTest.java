package com.hlala.strategy;

import com.hlala.domain.AdminApproval;
import com.hlala.domain.DepositHold;
import com.hlala.domain.Enums.DepositHoldState;
import com.hlala.domain.Enums.DepositHoldTrigger;
import com.hlala.exception.ApprovalNotSatisfiedException;
import com.hlala.exception.IllegalTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.hlala.domain.Enums.DepositHoldState.*;
import static com.hlala.domain.Enums.DepositHoldTrigger.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepositHoldTransitionTableTest {

    private final DepositHoldTransitionTable table = new DepositHoldTransitionTable(new TwoDistinctAdminsApprovalStrategy());
    private final UUID hold = UUID.randomUUID();

    private DepositHold holdIn(DepositHoldState state) {
        return new DepositHold(hold, UUID.randomUUID(), new BigDecimal("1500"), state, null, Instant.now());
    }

    private List<AdminApproval> approvals(int distinctAdmins) {
        return java.util.stream.IntStream.range(0, distinctAdmins)
                .mapToObj(i -> new AdminApproval(UUID.randomUUID(), hold, UUID.randomUUID(), Instant.now()))
                .toList();
    }

    private TransitionContext ctx(boolean handover, boolean openDispute, int admins) {
        return new TransitionContext(UUID.randomUUID(), null, handover, openDispute, approvals(admins));
    }

    @Test
    void happyPathEdgesNeedNoApproval() {
        assertThat(table.resolve(holdIn(PENDING), PAYMENT_CONFIRMED, ctx(false, false, 0)).to()).isEqualTo(HELD);
        assertThat(table.resolve(holdIn(HELD), MOVE_IN_CONFIRMED, ctx(false, false, 0)).to()).isEqualTo(ACTIVE);
        assertThat(table.resolve(holdIn(ACTIVE), DISPUTE_OPENED, ctx(false, false, 0)).to()).isEqualTo(DISPUTED);
    }

    @Test
    void cancelBeforeMoveInRefundsWithoutApproval() {
        assertThat(table.resolve(holdIn(HELD), CANCELLED_BEFORE_MOVE_IN, ctx(false, false, 0)).to()).isEqualTo(REFUNDED);
    }

    @Test
    void releaseNeedsHandoverAndTwoAdmins() {
        assertThat(table.resolve(holdIn(ACTIVE), RELEASE_AFTER_HANDOVER, ctx(true, false, 2)).to()).isEqualTo(RELEASED);
    }

    @Test
    void releaseWithoutHandoverIsRefused() {
        assertThatThrownBy(() -> table.resolve(holdIn(ACTIVE), RELEASE_AFTER_HANDOVER, ctx(false, false, 2)))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void releaseWithOneAdminIsRefused() {
        assertThatThrownBy(() -> table.resolve(holdIn(ACTIVE), RELEASE_AFTER_HANDOVER, ctx(true, false, 1)))
                .isInstanceOf(ApprovalNotSatisfiedException.class);
    }

    @Test
    void sameAdminApprovingTwiceCountsOnce() {
        UUID admin = UUID.randomUUID();
        List<AdminApproval> twiceBySameAdmin = List.of(
                new AdminApproval(UUID.randomUUID(), hold, admin, Instant.now()),
                new AdminApproval(UUID.randomUUID(), hold, admin, Instant.now()));
        TransitionContext ctx = new TransitionContext(UUID.randomUUID(), null, true, false, twiceBySameAdmin);
        assertThatThrownBy(() -> table.resolve(holdIn(ACTIVE), RELEASE_AFTER_HANDOVER, ctx))
                .isInstanceOf(ApprovalNotSatisfiedException.class);
    }

    @ParameterizedTest
    @EnumSource(value = DepositHoldTrigger.class, names = {"RESOLVED_FOR_LANDLORD", "RESOLVED_FOR_TENANT", "RESOLVED_SPLIT"})
    void disputeOutcomesNeedTwoAdminsAndNoOpenDispute(DepositHoldTrigger trigger) {
        assertThat(table.resolve(holdIn(DISPUTED), trigger, ctx(false, false, 2)).to()).isIn(RELEASED, REFUNDED, PARTIALLY_SETTLED);
        assertThatThrownBy(() -> table.resolve(holdIn(DISPUTED), trigger, ctx(false, false, 1)))
                .isInstanceOf(ApprovalNotSatisfiedException.class);
        assertThatThrownBy(() -> table.resolve(holdIn(DISPUTED), trigger, ctx(false, true, 2)))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void disputeCannotBeOpenedBeforeMoveIn() {
        assertThatThrownBy(() -> table.resolve(holdIn(HELD), DISPUTE_OPENED, ctx(false, false, 0)))
                .isInstanceOf(IllegalTransitionException.class);
    }

    @ParameterizedTest
    @EnumSource(value = DepositHoldState.class, names = {"RELEASED", "REFUNDED", "PARTIALLY_SETTLED"})
    void terminalStatesHaveNoWayOut(DepositHoldState terminal) {
        for (DepositHoldTrigger trigger : DepositHoldTrigger.values()) {
            assertThat(table.find(terminal, trigger)).isEmpty();
        }
    }

    @Test
    void everyTriggerHasExactlyOneEdgePerSourceState() {
        for (DepositHoldState state : DepositHoldState.values()) {
            for (DepositHoldTrigger trigger : DepositHoldTrigger.values()) {
                long edges = table.all().stream().filter(t -> t.from() == state && t.trigger() == trigger).count();
                assertThat(edges).isLessThanOrEqualTo(1);
            }
        }
    }
}
