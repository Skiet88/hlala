package com.hlala.repository;

import com.hlala.domain.DepositHoldEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepositHoldEventRepository extends JpaRepository<DepositHoldEvent, UUID> {
    List<DepositHoldEvent> findByDepositHoldIdOrderByOccurredAtAsc(UUID depositHoldId);

    List<DepositHoldEvent> findTop200ByOrderByOccurredAtDesc();
}
