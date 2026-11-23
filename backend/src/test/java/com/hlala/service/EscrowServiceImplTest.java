package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.IllegalTransitionException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.EscrowRecordFactory;
import com.hlala.repository.*;
import com.hlala.strategy.DepositHoldTransitionTable;
import com.hlala.strategy.TwoDistinctAdminsApprovalStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EscrowServiceImplTest {

    @Mock DepositHoldRepository holds;
    @Mock DepositHoldEventRepository events;
    @Mock AdminApprovalRepository approvals;
    @Mock DisputeRepository disputes;
    @Mock LeaseRepository leases;
    @Mock ProfileLookup lookup;
    @Mock NotificationService notifications;

    private EscrowServiceImpl escrow;
    private DepositHold current;
    private final List<AdminApproval> savedApprovals = new ArrayList<>();
    private final List<DepositHoldEvent> savedEvents = new ArrayList<>();
    private final UUID holdId = UUID.randomUUID();
    private final Lease lease = new Lease(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            new BigDecimal("1500"), LocalDate.now(), null, Enums.LeaseLanguage.EN,
            Enums.SignatureStatus.SIGNED, Enums.SignatureStatus.SIGNED);

    private final Actor adminA = new Actor(UUID.randomUUID(), Set.of(Enums.Role.ADMIN));
    private final Actor adminB = new Actor(UUID.randomUUID(), Set.of(Enums.Role.ADMIN));
    private final AdminProfile profileA = new AdminProfile(UUID.randomUUID(), adminA.userId(), Instant.now());
    private final AdminProfile profileB = new AdminProfile(UUID.randomUUID(), adminB.userId(), Instant.now());

    @BeforeEach
    void setUp() {
        escrow = new EscrowServiceImpl(holds, events, approvals, disputes, leases,
                new DepositHoldTransitionTable(new TwoDistinctAdminsApprovalStrategy()),
                new TwoDistinctAdminsApprovalStrategy(), new EscrowRecordFactory(), lookup, notifications);

        // ACTIVE hold whose tenant has confirmed the handover; last event a moment ago.
        current = new DepositHold(holdId, lease.getId(), new BigDecimal("1500"), Enums.DepositHoldState.ACTIVE,
                Instant.now().minusSeconds(5), Instant.now().minusSeconds(3600));
        savedEvents.add(new DepositHoldEvent(UUID.randomUUID(), holdId, Enums.DepositHoldState.ACTIVE,
                Enums.DepositHoldState.ACTIVE, UUID.randomUUID(), "Tenant confirmed key handover",
                Instant.now().minusSeconds(5)));

        when(holds.findById(holdId)).thenAnswer(i -> Optional.of(current));
        when(holds.save(any(DepositHold.class))).thenAnswer(i -> current = i.getArgument(0));
        when(events.findByDepositHoldIdOrderByOccurredAtAsc(holdId)).thenAnswer(i -> List.copyOf(savedEvents));
        when(events.save(any(DepositHoldEvent.class))).thenAnswer(i -> {
            savedEvents.add(i.getArgument(0));
            return i.getArgument(0);
        });
        when(approvals.findByDepositHoldId(holdId)).thenAnswer(i -> List.copyOf(savedApprovals));
        when(approvals.save(any(AdminApproval.class))).thenAnswer(i -> {
            savedApprovals.add(i.getArgument(0));
            return i.getArgument(0);
        });
        when(disputes.findByDepositHoldIdAndStatus(any(), any())).thenReturn(List.of());
        when(leases.findById(lease.getId())).thenReturn(Optional.of(lease));
        when(lookup.admin(adminA)).thenReturn(profileA);
        when(lookup.admin(adminB)).thenReturn(profileB);
        when(lookup.isPartyTo(any(), any())).thenReturn(false);
        when(lookup.userIdOfTenant(any())).thenReturn(UUID.randomUUID());
        when(lookup.userIdOfLandlord(any())).thenReturn(UUID.randomUUID());
    }

    @Test
    void oneApprovalDoesNotMoveMoney() {
        escrow.approve(adminA, holdId);
        assertThat(current.getState()).isEqualTo(Enums.DepositHoldState.ACTIVE);
    }

    @Test
    void secondDistinctAdminReleasesAndLogsIt() {
        escrow.approve(adminA, holdId);
        escrow.approve(adminB, holdId);

        assertThat(current.getState()).isEqualTo(Enums.DepositHoldState.RELEASED);
        DepositHoldEvent last = savedEvents.get(savedEvents.size() - 1);
        assertThat(last.getFromState()).isEqualTo(Enums.DepositHoldState.ACTIVE);
        assertThat(last.getToState()).isEqualTo(Enums.DepositHoldState.RELEASED);
        assertThat(last.getTriggeredByUserId()).isEqualTo(adminB.userId());
    }

    @Test
    void sameAdminCannotApproveTwice() {
        escrow.approve(adminA, holdId);
        assertThatThrownBy(() -> escrow.approve(adminA, holdId)).isInstanceOf(ValidationException.class);
        assertThat(current.getState()).isEqualTo(Enums.DepositHoldState.ACTIVE);
    }

    @Test
    void adminWhoIsPartyToTheLeaseCannotApprove() {
        when(lookup.isPartyTo(adminA, lease)).thenReturn(true);
        assertThatThrownBy(() -> escrow.approve(adminA, holdId)).isInstanceOf(ForbiddenOperationException.class);
        verify(approvals, never()).save(any());
    }

    @Test
    void nothingToApproveBeforeHandover() {
        current = current.withHandoverConfirmed(null);
        assertThatThrownBy(() -> escrow.approve(adminA, holdId)).isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void nothingToApproveWhileADisputeIsOpen() {
        when(disputes.findByDepositHoldIdAndStatus(holdId, Enums.DisputeStatus.OPEN))
                .thenReturn(List.of(mock(Dispute.class)));
        assertThatThrownBy(() -> escrow.approve(adminA, holdId)).isInstanceOf(IllegalTransitionException.class);
    }

    @Test
    void approvalsFromBeforeTheLatestEventAreIgnored() {
        // Admin A approved BEFORE the latest event: it must not count toward this payout.
        savedApprovals.add(new AdminApproval(UUID.randomUUID(), holdId, profileA.getId(), Instant.now().minusSeconds(60)));
        escrow.approve(adminB, holdId);
        assertThat(current.getState()).isEqualTo(Enums.DepositHoldState.ACTIVE);
    }
}
