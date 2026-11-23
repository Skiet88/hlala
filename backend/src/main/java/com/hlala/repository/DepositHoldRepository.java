package com.hlala.repository;

import com.hlala.domain.DepositHold;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DepositHoldRepository extends JpaRepository<DepositHold, UUID> {
    Optional<DepositHold> findByLeaseId(UUID leaseId);

    /**
     * Loads the hold and bumps its version when the transaction commits, even
     * if nothing on the hold itself changed. Used where a decision about the
     * hold is made from other rows (approvals), so two such decisions on the
     * same hold cannot both commit from the same snapshot.
     */
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select h from DepositHold h where h.id = :id")
    Optional<DepositHold> findForDecisionById(@Param("id") UUID id);
}
