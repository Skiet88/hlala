package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.PaymentFactory;
import com.hlala.repository.*;
import com.hlala.strategy.MeterSplitStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Chooses the split strategy by mode (Strategy pattern): adding a new way of
 * splitting means adding a MeterSplitStrategy bean, not editing this class.
 */
@Service
public class MeterServiceImpl implements MeterService {

    private static final Set<Enums.DepositHoldState> OCCUPIED =
            EnumSet.of(Enums.DepositHoldState.ACTIVE, Enums.DepositHoldState.DISPUTED);

    private final Map<Enums.SplitMode, MeterSplitStrategy> strategies = new EnumMap<>(Enums.SplitMode.class);
    private final RoomRepository rooms;
    private final LeaseRepository leases;
    private final DepositHoldRepository holds;
    private final MeterReadingRepository readings;
    private final MeterSplitRepository splits;
    private final PaymentFactory factory;
    private final ProfileLookup lookup;

    public MeterServiceImpl(List<MeterSplitStrategy> strategyBeans, RoomRepository rooms, LeaseRepository leases,
                            DepositHoldRepository holds, MeterReadingRepository readings,
                            MeterSplitRepository splits, PaymentFactory factory, ProfileLookup lookup) {
        for (MeterSplitStrategy s : strategyBeans) {
            strategies.put(s.mode(), s);
        }
        this.rooms = rooms;
        this.leases = leases;
        this.holds = holds;
        this.readings = readings;
        this.splits = splits;
        this.factory = factory;
        this.lookup = lookup;
    }

    @Override
    @Transactional
    public TopUpResult recordTopUp(Actor actor, UUID roomId, BigDecimal amount, Enums.SplitMode mode,
                                   Map<UUID, Integer> occupantsByLease) {
        Room room = rooms.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
        List<Lease> active = leases.findByRoomId(roomId).stream()
                .filter(l -> holds.findByLeaseId(l.getId()).map(h -> OCCUPIED.contains(h.getState())).orElse(false))
                .toList();

        boolean allowed = lookup.isLandlordOf(actor, room) || active.stream().anyMatch(l -> lookup.isTenantOf(actor, l));
        if (!allowed) {
            throw new ForbiddenOperationException("Only the landlord or a current tenant can record a top-up");
        }
        MeterSplitStrategy strategy = strategies.get(mode);
        if (strategy == null) {
            throw new ValidationException("Unknown split mode: " + mode);
        }

        MeterReading reading = readings.save(factory.topUp(roomId, amount));
        List<MeterSplit> saved = new ArrayList<>();
        strategy.split(amount, active, occupantsByLease).forEach((leaseId, share) ->
                saved.add(splits.save(factory.split(reading.getId(), leaseId, share, mode))));
        return new TopUpResult(reading, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterSplit> splitsForLease(Actor party, UUID leaseId) {
        Lease lease = leases.findById(leaseId).orElseThrow(() -> new NotFoundException("Lease not found"));
        lookup.requirePartyTo(party, lease);
        return splits.findByLeaseId(leaseId);
    }
}
