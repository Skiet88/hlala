package com.hlala.repository;

import com.hlala.domain.AdminApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminApprovalRepository extends JpaRepository<AdminApproval, UUID> {
    List<AdminApproval> findByDepositHoldId(UUID depositHoldId);
}
