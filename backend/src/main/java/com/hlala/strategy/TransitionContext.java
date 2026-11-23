package com.hlala.strategy;

import com.hlala.domain.AdminApproval;

import java.util.List;
import java.util.UUID;

/**
 * Everything a transition guard may need, gathered by EscrowService before it
 * asks the transition table whether a move is legal. Guards never query
 * repositories themselves, which keeps them pure and trivially unit-testable.
 */
public record TransitionContext(
        UUID triggeredByUserId,
        String reason,
        boolean handoverConfirmed,
        boolean openDispute,
        List<AdminApproval> approvals
) {
}
