package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.PaymentFactory;
import com.hlala.repository.DepositHoldRepository;
import com.hlala.repository.LeaseRepository;
import com.hlala.repository.RentPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LeaseServiceImpl implements LeaseService {

    /** Rent is only recorded while someone is actually living there. */
    private static final Set<Enums.DepositHoldState> OCCUPIED =
            EnumSet.of(Enums.DepositHoldState.ACTIVE, Enums.DepositHoldState.DISPUTED);

    private final LeaseRepository leases;
    private final RentPaymentRepository payments;
    private final DepositHoldRepository holds;
    private final PaymentFactory paymentFactory;
    private final ProfileLookup lookup;
    private final NotificationService notifications;

    public LeaseServiceImpl(LeaseRepository leases, RentPaymentRepository payments, DepositHoldRepository holds,
                            PaymentFactory paymentFactory, ProfileLookup lookup,
                            NotificationService notifications) {
        this.leases = leases;
        this.payments = payments;
        this.holds = holds;
        this.paymentFactory = paymentFactory;
        this.lookup = lookup;
        this.notifications = notifications;
    }

    @Override
    @Transactional(readOnly = true)
    public Lease get(Actor party, UUID leaseId) {
        Lease lease = find(leaseId);
        lookup.requirePartyTo(party, lease);
        return lease;
    }

    @Override
    @Transactional
    public Lease sign(Actor party, UUID leaseId) {
        Lease lease = find(leaseId);
        Lease signed;
        if (lookup.isTenantOf(party, lease)) {
            signed = lease.withTenantSignature(Enums.SignatureStatus.SIGNED);
        } else if (lookup.isLandlordOf(party, lease)) {
            signed = lease.withLandlordSignature(Enums.SignatureStatus.SIGNED);
        } else {
            throw new ForbiddenOperationException("You are not a party to this lease");
        }
        return leases.save(signed);
    }

    @Override
    @Transactional
    public RentPayment recordPayment(Actor landlord, UUID leaseId, BigDecimal amount, Enums.PaymentMethod method,
                                     YearMonth coveredPeriod) {
        Lease lease = find(leaseId);
        if (!lookup.isLandlordOf(landlord, lease)) {
            throw new ForbiddenOperationException("Only the landlord records rent on this lease");
        }
        DepositHold hold = holds.findByLeaseId(leaseId)
                .orElseThrow(() -> new NotFoundException("No deposit hold for this lease"));
        if (!OCCUPIED.contains(hold.getState())) {
            throw new ValidationException("Rent can only be recorded once the tenant has moved in");
        }
        RentPayment payment = payments.save(
                paymentFactory.rent(leaseId, amount, method, coveredPeriod, landlord.userId()));
        notifications.notify(lookup.userIdOfTenant(lease.getTenantId()),
                Enums.NotificationType.PAYMENT_RECEIVED, payment.getId().toString());
        return payment;
    }

    @Override
    @Transactional(readOnly = true)
    public RentBook rentBook(Actor party, UUID leaseId) {
        Lease lease = get(party, leaseId);
        List<RentPayment> all = payments.findByLeaseIdOrderByCoveredPeriodAsc(leaseId);
        BigDecimal collected = all.stream().map(RentPayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expected = lease.getMonthlyRent().multiply(BigDecimal.valueOf(monthsDue(lease)));
        BigDecimal owed = expected.subtract(collected).max(BigDecimal.ZERO);
        return new RentBook(lease, all, collected, owed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomeEntry> tenantHome(Actor tenant) {
        TenantProfile profile = lookup.tenant(tenant);
        return leases.findByTenantId(profile.getId()).stream()
                .map(l -> {
                    DepositHold hold = holds.findByLeaseId(l.getId()).orElse(null);
                    List<RentPayment> recent = payments.findByLeaseIdOrderByCoveredPeriodAsc(l.getId());
                    int from = Math.max(0, recent.size() - 6);
                    return new HomeEntry(l, hold, List.copyOf(recent.subList(from, recent.size())));
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lease> landlordLeases(Actor landlord) {
        return leases.findByLandlordId(lookup.landlord(landlord).getId());
    }

    /** Months from the start month up to this month (or the end month, if earlier), inclusive. */
    static long monthsDue(Lease lease) {
        YearMonth start = YearMonth.from(lease.getStartDate());
        YearMonth now = YearMonth.from(LocalDate.now());
        YearMonth last = lease.getEndDate() == null ? now
                : (YearMonth.from(lease.getEndDate()).isBefore(now) ? YearMonth.from(lease.getEndDate()) : now);
        if (last.isBefore(start)) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(start, last) + 1;
    }

    private Lease find(UUID leaseId) {
        return leases.findById(leaseId).orElseThrow(() -> new NotFoundException("Lease not found"));
    }
}
