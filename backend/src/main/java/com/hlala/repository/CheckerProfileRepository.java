package com.hlala.repository;

import com.hlala.domain.CheckerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CheckerProfileRepository extends JpaRepository<CheckerProfile, UUID> {
    Optional<CheckerProfile> findByUserId(UUID userId);
}
