package com.hlala.repository;

import com.hlala.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByListingId(UUID listingId);

    List<Application> findByTenantId(UUID tenantId);
}
