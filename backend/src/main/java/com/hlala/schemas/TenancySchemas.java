package com.hlala.schemas;

import com.hlala.domain.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Applications, leases, rent and escrow views. */
public final class TenancySchemas {

    private TenancySchemas() {
    }

    // --- applications ---------------------------------------------------

    public record DecisionRequest(boolean accept) {
    }

    /** Verification TICKS only. No documents, no ID numbers - by design, not by omission. */
    public record ApplicationDto(UUID id, UUID listingId, boolean idVerified, boolean incomeVerified,
                                 boolean referencesChecked, Enums.ApplicationStatus status, Instant appliedAt) {
        public static ApplicationDto of(Application a) {
            return new ApplicationDto(a.getId(), a.getListingId(), a.isIdVerified(), a.isIncomeVerified(),
                    a.isReferencesChecked(), a.getStatus(), a.getAppliedAt());
        }
    }

    // --- starting a tenancy ---------------------------------------------

    public record StartTenancyRequest(@NotNull LocalDate startDate, @NotNull Enums.LeaseLanguage language) {
    }

    public record StartedTenancyDto(LeaseDto lease, DepositHoldDto hold) {
    }

    // --- leases and rent ------------------------------------------------

    public record LeaseDto(UUID id, UUID roomId, BigDecimal monthlyRent, LocalDate startDate, LocalDate endDate,
                           Enums.LeaseLanguage language, Enums.SignatureStatus tenantSignature,
                           Enums.SignatureStatus landlordSignature, boolean fullySigned) {
        public static LeaseDto of(Lease l) {
            return new LeaseDto(l.getId(), l.getRoomId(), l.getMonthlyRent(), l.getStartDate(), l.getEndDate(),
                    l.getLanguage(), l.getTenantSignature(), l.getLandlordSignature(), l.isFullySigned());
        }
    }

    public record RecordPaymentRequest(@NotNull @DecimalMin("1") BigDecimal amount,
                                       @NotNull Enums.PaymentMethod method,
                                       @NotNull YearMonth coveredPeriod) {
    }

    public record RentPaymentDto(UUID id, BigDecimal amount, Enums.PaymentMethod method,
                                 YearMonth coveredPeriod, Instant recordedAt) {
        public static RentPaymentDto of(RentPayment p) {
            return new RentPaymentDto(p.getId(), p.getAmount(), p.getMethod(), p.getCoveredPeriod(), p.getRecordedAt());
        }
    }

    public record RentBookDto(UUID leaseId, BigDecimal monthlyRent, BigDecimal collected, BigDecimal owed,
                              List<RentPaymentDto> payments) {
    }

    public record HomeEntryDto(LeaseDto lease, DepositHoldDto deposit, List<RentPaymentDto> recentPayments) {
    }

    // --- escrow ---------------------------------------------------------

    /**
     * What a tenant or landlord sees about their deposit: the state and amount.
     * Never the event history - that names admins and is admin-only.
     */
    public record DepositHoldDto(UUID id, UUID leaseId, BigDecimal amount, Enums.DepositHoldState state,
                                 boolean handoverConfirmed) {
        public static DepositHoldDto of(DepositHold h) {
            return h == null ? null
                    : new DepositHoldDto(h.getId(), h.getLeaseId(), h.getAmount(), h.getState(),
                    h.getHandoverConfirmedAt() != null);
        }
    }

    public record DepositHoldEventDto(UUID id, UUID depositHoldId, Enums.DepositHoldState fromState,
                                      Enums.DepositHoldState toState, UUID triggeredByUserId, String reason,
                                      Instant occurredAt) {
        public static DepositHoldEventDto of(DepositHoldEvent e) {
            return new DepositHoldEventDto(e.getId(), e.getDepositHoldId(), e.getFromState(), e.getToState(),
                    e.getTriggeredByUserId(), e.getReason(), e.getOccurredAt());
        }
    }

    public record AdminApprovalDto(UUID id, UUID depositHoldId, UUID adminProfileId, Instant approvedAt) {
        public static AdminApprovalDto of(AdminApproval a) {
            return new AdminApprovalDto(a.getId(), a.getDepositHoldId(), a.getAdminProfileId(), a.getApprovedAt());
        }
    }

    /**
     * Admin-only. A separate class from DepositHoldDto on purpose: a controller
     * cannot leak the audit view to a tenant by forgetting an if-statement,
     * because the tenant endpoints never construct this type.
     */
    public record DepositHoldAuditDto(DepositHoldDto hold, List<DepositHoldEventDto> history,
                                      List<AdminApprovalDto> currentApprovals) {
    }
}
