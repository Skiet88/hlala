package com.hlala.factory;

import com.hlala.domain.Enums;
import com.hlala.domain.Listing;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class ListingFactory {

    public Listing create(UUID roomId, BigDecimal rentAmount, BigDecimal depositAmount) {
        if (roomId == null) {
            throw new ValidationException("A listing must reference a room");
        }
        requirePositive(rentAmount, "Rent amount");
        requirePositive(depositAmount, "Deposit amount");

        return new Listing(UUID.randomUUID(), roomId, rentAmount, depositAmount,
                Enums.ListingStatus.DRAFT, Enums.VerificationStatus.UNVERIFIED, null, Instant.now());
    }

    private void requirePositive(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0) {
            throw new ValidationException(label + " must be greater than zero");
        }
    }
}
