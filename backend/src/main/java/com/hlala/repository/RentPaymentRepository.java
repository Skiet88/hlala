package com.hlala.repository;

import com.hlala.domain.RentPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RentPaymentRepository extends JpaRepository<RentPayment, UUID> {
    List<RentPayment> findByLeaseIdOrderByCoveredPeriodAsc(UUID leaseId);
}
