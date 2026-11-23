package com.hlala.repository;

import com.hlala.domain.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantProfileRepository extends JpaRepository<TenantProfile, UUID> {
    Optional<TenantProfile> findByUserId(UUID userId);
}
