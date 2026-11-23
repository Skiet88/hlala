package com.hlala.repository;

import com.hlala.domain.RepairTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepairTicketRepository extends JpaRepository<RepairTicket, UUID> {
    List<RepairTicket> findByLeaseId(UUID leaseId);
}
