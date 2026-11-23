package com.hlala.repository;

import com.hlala.domain.MeterSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeterSplitRepository extends JpaRepository<MeterSplit, UUID> {
    List<MeterSplit> findByMeterReadingId(UUID meterReadingId);

    List<MeterSplit> findByLeaseId(UUID leaseId);
}
