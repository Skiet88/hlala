package com.hlala.factory;

import com.hlala.domain.Application;
import com.hlala.domain.Enums;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ApplicationFactory {

    public Application create(UUID listingId, UUID tenantId, boolean idVerified, boolean incomeVerified,
                               boolean referencesChecked) {
        if (listingId == null) {
            throw new ValidationException("An application must reference a listing");
        }
        if (tenantId == null) {
            throw new ValidationException("An application must reference a tenant");
        }
        return new Application(UUID.randomUUID(), listingId, tenantId, idVerified, incomeVerified,
                referencesChecked, Enums.ApplicationStatus.PENDING, Instant.now());
    }
}
