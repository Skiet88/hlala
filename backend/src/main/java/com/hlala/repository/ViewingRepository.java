package com.hlala.repository;

import com.hlala.domain.Viewing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ViewingRepository extends JpaRepository<Viewing, UUID> {
    List<Viewing> findByTenantId(UUID tenantId);

    List<Viewing> findByListingId(UUID listingId);
}
