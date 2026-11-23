package com.hlala.api;

import com.hlala.config.CurrentActor;
import com.hlala.schemas.ListingSchemas.SavedListingDto;
import com.hlala.schemas.TenancySchemas.*;
import com.hlala.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Apply -> pay -> move in -> live there -> hand back keys. */
@RestController
public class TenancyController {

    private final ApplicationService applications;
    private final TenancyOnboardingFacade onboarding;
    private final EscrowService escrow;
    private final LeaseService leases;
    private final ListingService listings;

    public TenancyController(ApplicationService applications, TenancyOnboardingFacade onboarding,
                             EscrowService escrow, LeaseService leases, ListingService listings) {
        this.applications = applications;
        this.onboarding = onboarding;
        this.escrow = escrow;
        this.leases = leases;
        this.listings = listings;
    }

    // --- applications ---------------------------------------------------

    @GetMapping("/tenants/me/applications")
    @PreAuthorize("hasRole('TENANT')")
    public List<ApplicationDto> myApplications(@CurrentActor Actor actor) {
        return applications.mine(actor).stream().map(ApplicationDto::of).toList();
    }

    @GetMapping("/tenants/me/saved")
    @PreAuthorize("hasRole('TENANT')")
    public List<SavedListingDto> saved(@CurrentActor Actor actor) {
        return listings.saved(actor).stream().map(SavedListingDto::of).toList();
    }

    @PostMapping("/applications/{id}/decision")
    @PreAuthorize("hasRole('LANDLORD')")
    public ApplicationDto decide(@CurrentActor Actor actor, @PathVariable UUID id, @RequestBody DecisionRequest req) {
        return ApplicationDto.of(applications.decide(actor, id, req.accept()));
    }

    @PostMapping("/applications/{id}/withdraw")
    @PreAuthorize("hasRole('TENANT')")
    public ApplicationDto withdraw(@CurrentActor Actor actor, @PathVariable UUID id) {
        return ApplicationDto.of(applications.withdraw(actor, id));
    }

    /** Accepted application -> lease + deposit hold (PENDING until payment clears). */
    @PostMapping("/applications/{id}/tenancy")
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public StartedTenancyDto startTenancy(@CurrentActor Actor actor, @PathVariable UUID id,
                                          @Valid @RequestBody StartTenancyRequest req) {
        TenancyOnboardingFacade.StartedTenancy started = onboarding.startTenancy(actor, id, req.startDate(), req.language());
        return new StartedTenancyDto(LeaseDto.of(started.lease()), DepositHoldDto.of(started.hold()));
    }

    // --- deposit (party view: state only, never the audit trail) --------

    @GetMapping("/deposit-holds/{id}")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public DepositHoldDto hold(@CurrentActor Actor actor, @PathVariable UUID id) {
        return DepositHoldDto.of(escrow.getForParty(actor, id));
    }

    @PostMapping("/deposit-holds/{id}/confirm-moved-in")
    @PreAuthorize("hasRole('TENANT')")
    public DepositHoldDto confirmMovedIn(@CurrentActor Actor actor, @PathVariable UUID id) {
        return DepositHoldDto.of(onboarding.confirmMoveIn(actor, id));
    }

    @PostMapping("/deposit-holds/{id}/confirm-handover")
    @PreAuthorize("hasRole('TENANT')")
    public DepositHoldDto confirmHandover(@CurrentActor Actor actor, @PathVariable UUID id) {
        return DepositHoldDto.of(escrow.confirmHandover(actor, id));
    }

    @PostMapping("/deposit-holds/{id}/cancel")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public DepositHoldDto cancel(@CurrentActor Actor actor, @PathVariable UUID id) {
        return DepositHoldDto.of(escrow.cancelBeforeMoveIn(actor, id));
    }

    // --- leases and rent -----------------------------------------------

    @GetMapping("/tenants/me/home")
    @PreAuthorize("hasRole('TENANT')")
    public List<HomeEntryDto> home(@CurrentActor Actor actor) {
        return leases.tenantHome(actor).stream()
                .map(e -> new HomeEntryDto(LeaseDto.of(e.lease()), DepositHoldDto.of(e.hold()),
                        e.recentPayments().stream().map(RentPaymentDto::of).toList()))
                .toList();
    }

    @GetMapping("/leases/{id}")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public LeaseDto lease(@CurrentActor Actor actor, @PathVariable UUID id) {
        return LeaseDto.of(leases.get(actor, id));
    }

    @PostMapping("/leases/{id}/sign")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public LeaseDto sign(@CurrentActor Actor actor, @PathVariable UUID id) {
        return LeaseDto.of(leases.sign(actor, id));
    }

    @PostMapping("/leases/{id}/rent-payments")
    @PreAuthorize("hasRole('LANDLORD')")
    @ResponseStatus(HttpStatus.CREATED)
    public RentPaymentDto recordPayment(@CurrentActor Actor actor, @PathVariable UUID id,
                                        @Valid @RequestBody RecordPaymentRequest req) {
        return RentPaymentDto.of(leases.recordPayment(actor, id, req.amount(), req.method(), req.coveredPeriod()));
    }

    @GetMapping("/leases/{id}/rent-book")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public RentBookDto rentBook(@CurrentActor Actor actor, @PathVariable UUID id) {
        LeaseService.RentBook book = leases.rentBook(actor, id);
        return new RentBookDto(book.lease().getId(), book.lease().getMonthlyRent(), book.collected(), book.owed(),
                book.payments().stream().map(RentPaymentDto::of).toList());
    }
}
