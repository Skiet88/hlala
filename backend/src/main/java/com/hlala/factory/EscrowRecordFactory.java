package com.hlala.factory;

import com.hlala.domain.AdminApproval;
import com.hlala.domain.DepositHoldEvent;
import com.hlala.domain.Enums;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/** Audit-trail records around a DepositHold: transition events and admin approvals. */
@Component
public class EscrowRecordFactory {

    public DepositHoldEvent event(UUID depositHoldId, Enums.DepositHoldState from, Enums.DepositHoldState to,
                                  UUID triggeredByUserId, String reason) {
        if (depositHoldId == null || from == null || to == null) {
            throw new ValidationException("An escrow event needs a hold and both states");
        }
        if (triggeredByUserId == null) {
            throw new ValidationException("Every escrow event must record who triggered it");
        }
        return new DepositHoldEvent(UUID.randomUUID(), depositHoldId, from, to, triggeredByUserId, reason, Instant.now());
    }

    public AdminApproval approval(UUID depositHoldId, UUID adminProfileId) {
        if (depositHoldId == null || adminProfileId == null) {
            throw new ValidationException("An approval needs a hold and an admin");
        }
        return new AdminApproval(UUID.randomUUID(), depositHoldId, adminProfileId, Instant.now());
    }
}
