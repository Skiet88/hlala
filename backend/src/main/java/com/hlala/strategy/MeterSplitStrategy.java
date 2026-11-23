package com.hlala.strategy;

import com.hlala.domain.Enums;
import com.hlala.domain.Lease;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One strategy per Enums.SplitMode - EqualPerRoomSplitStrategy / PerPersonSplitStrategy implement this.
 * occupantsByLease lets PER_PERSON weight shares by headcount; pass an empty map (or omit entries)
 * to default any lease to a single occupant.
 */
public interface MeterSplitStrategy {

    /** The split mode this strategy implements; used to build the mode -> strategy lookup. */
    Enums.SplitMode mode();

    /** Returns each active lease's share of the top-up amount. Shares must sum to the total (rounding remainder goes to the first lease, by iteration order). */
    Map<UUID, BigDecimal> split(BigDecimal topUpAmount, List<Lease> activeLeasesOnRoom, Map<UUID, Integer> occupantsByLease);
}
