package com.hlala.strategy;

import com.hlala.domain.Enums;
import com.hlala.domain.Lease;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Weights each lease's share by its occupant count. Leases missing from occupantsByLease count as 1. */
@Component
public class PerPersonSplitStrategy implements MeterSplitStrategy {

    @Override
    public Enums.SplitMode mode() {
        return Enums.SplitMode.PER_PERSON;
    }

    @Override
    public Map<UUID, BigDecimal> split(BigDecimal topUpAmount, List<Lease> activeLeasesOnRoom,
                                        Map<UUID, Integer> occupantsByLease) {
        if (activeLeasesOnRoom == null || activeLeasesOnRoom.isEmpty()) {
            throw new ValidationException("Cannot split a top-up with no active leases on the room");
        }
        Map<UUID, Integer> occupants = occupantsByLease == null ? Map.of() : occupantsByLease;

        int totalPeople = 0;
        for (Lease lease : activeLeasesOnRoom) {
            int people = occupants.getOrDefault(lease.getId(), 1);
            if (people < 1) {
                throw new ValidationException("Occupant count must be at least 1 for lease " + lease.getId());
            }
            totalPeople += people;
        }

        BigDecimal perPerson = topUpAmount.divide(BigDecimal.valueOf(totalPeople), 2, RoundingMode.DOWN);
        Map<UUID, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal distributed = BigDecimal.ZERO;
        for (Lease lease : activeLeasesOnRoom) {
            int people = occupants.getOrDefault(lease.getId(), 1);
            BigDecimal share = perPerson.multiply(BigDecimal.valueOf(people));
            result.put(lease.getId(), share);
            distributed = distributed.add(share);
        }

        BigDecimal remainder = topUpAmount.subtract(distributed);
        UUID firstLeaseId = activeLeasesOnRoom.get(0).getId();
        result.put(firstLeaseId, result.get(firstLeaseId).add(remainder));
        return result;
    }
}
