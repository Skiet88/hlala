package com.hlala.factory;

import com.hlala.domain.DepositHold;
import com.hlala.domain.Enums;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The only way a DepositHold comes into existence. Always starts PENDING -
 * every other state is reached only through EscrowService transitions.
 */
@Component
public class DepositHoldFactory {

    public DepositHold open(UUID leaseId, BigDecimal amount) {
        if (leaseId == null) {
            throw new ValidationException("A deposit hold must reference a lease");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("Deposit amount must be greater than zero");
        }
        return new DepositHold(UUID.randomUUID(), leaseId, amount, Enums.DepositHoldState.PENDING, null, Instant.now());
    }
}
