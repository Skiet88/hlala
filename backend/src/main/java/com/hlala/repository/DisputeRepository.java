package com.hlala.repository;

import com.hlala.domain.Dispute;
import com.hlala.domain.Enums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    List<Dispute> findByDepositHoldId(UUID depositHoldId);

    List<Dispute> findByDepositHoldIdAndStatus(UUID depositHoldId, Enums.DisputeStatus status);

    List<Dispute> findByStatus(Enums.DisputeStatus status);
}
