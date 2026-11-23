package com.hlala.service;

import com.hlala.domain.Application;

import java.util.List;
import java.util.UUID;

public interface ApplicationService {

    Application apply(Actor tenant, UUID listingId);

    List<Application> forListing(Actor landlord, UUID listingId);

    List<Application> mine(Actor tenant);

    Application decide(Actor landlord, UUID applicationId, boolean accept);

    Application withdraw(Actor tenant, UUID applicationId);

    Application get(UUID applicationId);
}
