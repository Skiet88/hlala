package com.hlala.repository;

import com.hlala.domain.SavedListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavedListingRepository extends JpaRepository<SavedListing, UUID> {
    List<SavedListing> findByTenantId(UUID tenantId);
}
