package com.hlala.repository;

import com.hlala.domain.LandlordProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LandlordProfileRepository extends JpaRepository<LandlordProfile, UUID> {
    Optional<LandlordProfile> findByUserId(UUID userId);
}
