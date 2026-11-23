package com.hlala.factory;

import com.hlala.domain.AdminProfile;
import com.hlala.domain.CheckerProfile;
import com.hlala.domain.LandlordProfile;
import com.hlala.domain.TenantProfile;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ProfileFactory {

    public TenantProfile tenant(UUID userId) {
        requireUser(userId);
        return new TenantProfile(UUID.randomUUID(), userId, Instant.now());
    }

    public LandlordProfile landlord(UUID userId) {
        requireUser(userId);
        return new LandlordProfile(UUID.randomUUID(), userId, Instant.now());
    }

    public CheckerProfile checker(UUID userId) {
        requireUser(userId);
        return new CheckerProfile(UUID.randomUUID(), userId, Instant.now());
    }

    public AdminProfile admin(UUID userId) {
        requireUser(userId);
        return new AdminProfile(UUID.randomUUID(), userId, Instant.now());
    }

    private void requireUser(UUID userId) {
        if (userId == null) {
            throw new ValidationException("A profile must belong to a user");
        }
    }
}
