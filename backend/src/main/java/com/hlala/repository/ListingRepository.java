package com.hlala.repository;

import com.hlala.domain.Enums;
import com.hlala.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByStatus(Enums.ListingStatus status);

    List<Listing> findByRoomId(UUID roomId);

    List<Listing> findByVerificationStatus(Enums.VerificationStatus verificationStatus);
}
