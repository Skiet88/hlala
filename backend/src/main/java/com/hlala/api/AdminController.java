package com.hlala.api;

import com.hlala.config.CurrentActor;
import com.hlala.domain.DepositHold;
import com.hlala.schemas.AccountSchemas.GrantRoleRequest;
import com.hlala.schemas.TenancySchemas.*;
import com.hlala.schemas.TrustSchemas.DisputeDto;
import com.hlala.schemas.TrustSchemas.ResolveDisputeRequest;
import com.hlala.service.AccountService;
import com.hlala.service.Actor;
import com.hlala.service.EscrowService;
import com.hlala.service.TrustService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Back office. Resolving a dispute and approving a payout are separate
 * endpoints on purpose: deciding what happened and agreeing that money moves
 * are two actions, and the payout needs two different admins.
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TrustService trust;
    private final EscrowService escrow;
    private final AccountService accounts;

    public AdminController(TrustService trust, EscrowService escrow, AccountService accounts) {
        this.trust = trust;
        this.escrow = escrow;
        this.accounts = accounts;
    }

    @PostMapping("/disputes/{id}/resolve")
    public DisputeDto resolve(@CurrentActor Actor actor, @PathVariable UUID id,
                              @Valid @RequestBody ResolveDisputeRequest req) {
        return DisputeDto.of(trust.resolve(actor, id, req.resolution()));
    }

    @PostMapping("/deposit-holds/{id}/approvals")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositHoldAuditDto approve(@CurrentActor Actor actor, @PathVariable UUID id) {
        escrow.approve(actor, id);
        return audit(id);
    }

    /** Stand-in for the payment provider's webhook until one is integrated. */
    @PostMapping("/deposit-holds/{id}/payment-confirmed")
    public DepositHoldDto paymentConfirmed(@CurrentActor Actor actor, @PathVariable UUID id) {
        return DepositHoldDto.of(escrow.confirmPayment(actor, id));
    }

    @GetMapping("/admin/deposit-holds/{id}")
    public DepositHoldAuditDto audit(@PathVariable UUID id) {
        DepositHold hold = escrow.get(id);
        return new DepositHoldAuditDto(DepositHoldDto.of(hold),
                escrow.history(id).stream().map(DepositHoldEventDto::of).toList(),
                escrow.currentApprovals(id).stream().map(AdminApprovalDto::of).toList());
    }

    @GetMapping("/admin/money-trail")
    public List<DepositHoldEventDto> moneyTrail() {
        return escrow.moneyTrail().stream().map(DepositHoldEventDto::of).toList();
    }

    @GetMapping("/admin/disputes/open")
    public List<DisputeDto> openDisputes(@CurrentActor Actor actor) {
        return trust.openDisputes(actor).stream().map(DisputeDto::of).toList();
    }

    @PostMapping("/admin/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grantRole(@CurrentActor Actor actor, @Valid @RequestBody GrantRoleRequest req) {
        accounts.grantRole(actor, req.userId(), req.role());
    }
}
