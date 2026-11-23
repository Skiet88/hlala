package com.hlala.repository;

import com.hlala.domain.RoomCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomCheckRepository extends JpaRepository<RoomCheck, UUID> {
    List<RoomCheck> findByListingId(UUID listingId);

    List<RoomCheck> findByCheckerId(UUID checkerId);
}
