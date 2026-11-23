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

/** Splits a top-up equally across every active lease on the room, regardless of headcount. */
@Component
public class EqualPerRoomSplitStrategy implements MeterSplitStrategy {

    @Override
    public Enums.SplitMode mode() {
        return Enums.SplitMode.EQUAL_PER_ROOM;
    }

    @Override
    public Map<UUID, BigDecimal> split(BigDecimal topUpAmount, List<Lease> activeLeasesOnRoom,
                                        Map<UUID, Integer> occupantsByLease) {
        if (activeLeasesOnRoom == null || activeLeasesOnRoom.isEmpty()) {
            throw new ValidationException("Cannot split a top-up with no active leases on the room");
        }
        int count = activeLeasesOnRoom.size();
        BigDecimal share = topUpAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        BigDecimal distributed = share.multiply(BigDecimal.valueOf(count));
        BigDecimal remainder = topUpAmount.subtract(distributed);

        Map<UUID, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            BigDecimal amount = (i == 0) ? share.add(remainder) : share;
            result.put(activeLeasesOnRoom.get(i).getId(), amount);
        }
        return result;
    }
}
