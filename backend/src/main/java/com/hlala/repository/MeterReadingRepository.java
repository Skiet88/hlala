package com.hlala.repository;

import com.hlala.domain.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    List<MeterReading> findByRoomIdOrderByRecordedAtDesc(UUID roomId);
}
