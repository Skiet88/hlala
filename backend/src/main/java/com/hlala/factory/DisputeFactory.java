package com.hlala.factory;

import com.hlala.domain.Dispute;
import com.hlala.domain.Enums;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class DisputeFactory {

    public Dispute open(UUID depositHoldId, UUID raisedByUserId, List<URI> evidenceRefs) {
        if (depositHoldId == null) {
            throw new ValidationException("A dispute must reference a deposit hold");
        }
        if (raisedByUserId == null) {
            throw new ValidationException("A dispute must record who raised it");
        }
        return new Dispute(UUID.randomUUID(), depositHoldId, raisedByUserId, Enums.DisputeStatus.OPEN,
                null, evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs), Instant.now(), null);
    }
}
