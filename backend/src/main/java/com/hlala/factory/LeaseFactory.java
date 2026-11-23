package com.hlala.factory;

import com.hlala.domain.Enums;
import com.hlala.domain.Lease;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class LeaseFactory {

    public Lease create(UUID roomId, UUID tenantId, UUID landlordId,
                         BigDecimal monthlyRent, LocalDate startDate, LocalDate endDate,
                         Enums.LeaseLanguage language) {
        require(roomId != null, "A lease must reference a room");
        require(tenantId != null, "A lease must reference a tenant");
        require(landlordId != null, "A lease must reference a landlord");
        require(monthlyRent != null && monthlyRent.signum() > 0, "Monthly rent must be greater than zero");
        require(startDate != null, "Start date is required");
        require(endDate == null || !endDate.isBefore(startDate), "End date cannot be before start date");
        require(language != null, "Lease language is required");

        return new Lease(UUID.randomUUID(), roomId, tenantId, landlordId, monthlyRent,
                startDate, endDate, language, Enums.SignatureStatus.UNSIGNED, Enums.SignatureStatus.UNSIGNED);
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}
