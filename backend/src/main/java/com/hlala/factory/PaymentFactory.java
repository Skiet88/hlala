package com.hlala.factory;

import com.hlala.domain.Enums;
import com.hlala.domain.MeterReading;
import com.hlala.domain.MeterSplit;
import com.hlala.domain.RentPayment;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

/** Money records that are not escrow: rent payments and prepaid meter top-ups. */
@Component
public class PaymentFactory {

    public RentPayment rent(UUID leaseId, BigDecimal amount, Enums.PaymentMethod method,
                            YearMonth coveredPeriod, UUID recordedByUserId) {
        if (leaseId == null || recordedByUserId == null) {
            throw new ValidationException("A rent payment needs a lease and who recorded it");
        }
        requirePositive(amount, "Payment amount");
        if (method == null) {
            throw new ValidationException("Payment method is required");
        }
        if (coveredPeriod == null) {
            throw new ValidationException("The month this payment covers is required");
        }
        if (coveredPeriod.isAfter(YearMonth.now().plusMonths(3))) {
            throw new ValidationException("Rent cannot be recorded more than three months ahead");
        }
        return new RentPayment(UUID.randomUUID(), leaseId, amount, method, coveredPeriod, Instant.now(), recordedByUserId);
    }

    public MeterReading topUp(UUID roomId, BigDecimal amount) {
        if (roomId == null) {
            throw new ValidationException("A top-up must reference a room");
        }
        requirePositive(amount, "Top-up amount");
        return new MeterReading(UUID.randomUUID(), roomId, amount, Instant.now());
    }

    public MeterSplit split(UUID meterReadingId, UUID leaseId, BigDecimal share, Enums.SplitMode mode) {
        if (meterReadingId == null || leaseId == null || mode == null) {
            throw new ValidationException("A meter split needs a reading, a lease and a mode");
        }
        if (share == null || share.signum() < 0) {
            throw new ValidationException("A meter share cannot be negative");
        }
        return new MeterSplit(UUID.randomUUID(), meterReadingId, leaseId, share, mode);
    }

    private void requirePositive(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0) {
            throw new ValidationException(label + " must be greater than zero");
        }
    }
}
