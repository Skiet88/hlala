package com.hlala.service;

import com.hlala.domain.DepositHold;
import com.hlala.domain.Enums;
import com.hlala.domain.Lease;
import com.hlala.domain.RentPayment;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface LeaseService {

    record RentBook(Lease lease, List<RentPayment> payments, BigDecimal collected, BigDecimal owed) {
    }

    record HomeEntry(Lease lease, DepositHold hold, List<RentPayment> recentPayments) {
    }

    Lease get(Actor party, UUID leaseId);

    Lease sign(Actor party, UUID leaseId);

    RentPayment recordPayment(Actor landlord, UUID leaseId, BigDecimal amount, Enums.PaymentMethod method,
                              YearMonth coveredPeriod);

    RentBook rentBook(Actor party, UUID leaseId);

    List<HomeEntry> tenantHome(Actor tenant);

    List<Lease> landlordLeases(Actor landlord);
}
