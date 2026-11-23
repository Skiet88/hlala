package com.hlala.repository;

import com.hlala.domain.DepositHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepositHoldRepository extends JpaRepository<DepositHold, UUID> {
    Optional<DepositHold> findByLeaseId(UUID leaseId);
}
