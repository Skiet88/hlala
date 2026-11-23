package com.hlala.repository;

import com.hlala.domain.Lease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {
    List<Lease> findByTenantId(UUID tenantId);

    List<Lease> findByLandlordId(UUID landlordId);

    List<Lease> findByRoomId(UUID roomId);
}
