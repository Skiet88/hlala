package com.hlala.strategy;

import com.hlala.domain.AdminApproval;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The rule promised in the admin back office: no single staff member can
 * release a payout alone. Two approvals from two DIFFERENT admin profiles.
 * The same admin approving twice counts once.
 */
@Component
public class TwoDistinctAdminsApprovalStrategy implements PayoutApprovalStrategy {

    private static final int REQUIRED_DISTINCT_ADMINS = 2;

    @Override
    public boolean isSatisfied(List<AdminApproval> approvals) {
        if (approvals == null) {
            return false;
        }
        long distinctAdmins = approvals.stream()
                .map(AdminApproval::getAdminProfileId)
                .distinct()
                .count();
        return distinctAdmins >= REQUIRED_DISTINCT_ADMINS;
    }

    @Override
    public String describeRule() {
        return "A payout needs approval from " + REQUIRED_DISTINCT_ADMINS + " different admins";
    }
}
