package com.hlala.strategy;

import com.hlala.domain.AdminApproval;

import java.util.List;

/**
 * Decides whether enough approval exists to move money out of escrow.
 * Kept behind an interface so a third gate can be added later without
 * touching EscrowService's structure.
 */
public interface PayoutApprovalStrategy {

    boolean isSatisfied(List<AdminApproval> approvals);

    /** Human-readable rule, used in exception messages so a blocked payout says why. */
    String describeRule();
}
