package com.hlala.service;

import com.hlala.domain.Enums;
import com.hlala.domain.MeterReading;
import com.hlala.domain.MeterSplit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MeterService {

    record TopUpResult(MeterReading reading, List<MeterSplit> splits) {
    }

    TopUpResult recordTopUp(Actor actor, UUID roomId, BigDecimal amount, Enums.SplitMode mode,
                            Map<UUID, Integer> occupantsByLease);

    List<MeterSplit> splitsForLease(Actor party, UUID leaseId);
}
