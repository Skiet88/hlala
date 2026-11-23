package com.hlala.service;

import com.hlala.domain.*;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.exception.ValidationException;
import com.hlala.factory.CareFactory;
import com.hlala.factory.DisputeFactory;
import com.hlala.llm.FraudSignalScorer;
import com.hlala.repository.DisputeRepository;
import com.hlala.repository.LeaseRepository;
import com.hlala.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TrustServiceImpl implements TrustService {

    private final ReportRepository reports;
    private final DisputeRepository disputes;
    private final LeaseRepository leases;
    private final CareFactory careFactory;
    private final DisputeFactory disputeFactory;
    private final EscrowService escrow;
    private final FraudSignalScorer fraudScorer;
    private final ProfileLookup lookup;
    private final NotificationService notifications;

    public TrustServiceImpl(ReportRepository reports, DisputeRepository disputes, LeaseRepository leases,
                            CareFactory careFactory, DisputeFactory disputeFactory, EscrowService escrow,
                            FraudSignalScorer fraudScorer, ProfileLookup lookup,
                            NotificationService notifications) {
        this.reports = reports;
        this.disputes = disputes;
        this.leases = leases;
        this.careFactory = careFactory;
        this.disputeFactory = disputeFactory;
        this.escrow = escrow;
        this.fraudScorer = fraudScorer;
        this.lookup = lookup;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public ReportOutcome report(Actor actor, Enums.ReportReason reason, String description, List<URI> attachments) {
        Report report = reports.save(careFactory.report(actor.userId(), reason, description, attachments));
        boolean urgent = reason == Enums.ReportReason.SUSPECTED_SCAM || fraudScorer.isUrgent(description);
        if (urgent) {
            notifications.notifyAdmins(Enums.NotificationType.GENERAL, report.getId().toString());
        }
        return new ReportOutcome(report, urgent);
    }

    @Override
    @Transactional
    public Dispute openDispute(Actor party, UUID holdId, List<URI> evidence) {
        DepositHold hold = escrow.get(holdId);
        Lease lease = leaseOf(hold);
        lookup.requirePartyTo(party, lease);

        // Freeze first: if the hold is not ACTIVE this throws and nothing is saved.
        escrow.transition(holdId, Enums.DepositHoldTrigger.DISPUTE_OPENED, party.userId(), "Dispute opened");
        Dispute dispute = disputes.save(disputeFactory.open(holdId, party.userId(), evidence));
        notifications.notifyAdmins(Enums.NotificationType.DISPUTE_UPDATE, dispute.getId().toString());
        return dispute;
    }

    @Override
    @Transactional
    public Dispute addEvidence(Actor party, UUID disputeId, List<URI> evidence) {
        Dispute dispute = find(disputeId);
        lookup.requirePartyTo(party, leaseOf(escrow.get(dispute.getDepositHoldId())));
        if (dispute.getStatus() != Enums.DisputeStatus.OPEN) {
            throw new ValidationException("This dispute is closed");
        }
        if (evidence == null || evidence.isEmpty()) {
            throw new ValidationException("Attach at least one piece of evidence");
        }
        return disputes.save(dispute.withEvidence(evidence));
    }

    @Override
    @Transactional
    public Dispute resolve(Actor admin, UUID disputeId, Enums.DisputeResolution resolution) {
        lookup.admin(admin);
        if (resolution == null) {
            throw new ValidationException("A resolution is required");
        }
        Dispute dispute = find(disputeId);
        DepositHold hold = escrow.get(dispute.getDepositHoldId());
        Lease lease = leaseOf(hold);
        if (lookup.isPartyTo(admin, lease)) {
            throw new ForbiddenOperationException("You are a party to this lease, so you cannot decide its dispute");
        }
        if (dispute.getStatus() != Enums.DisputeStatus.OPEN) {
            throw new ValidationException("This dispute is already resolved");
        }

        Dispute resolved = disputes.save(dispute.resolved(resolution, Instant.now()));
        notifications.notify(lookup.userIdOfTenant(lease.getTenantId()),
                Enums.NotificationType.DISPUTE_UPDATE, resolved.getId().toString());
        notifications.notify(lookup.userIdOfLandlord(lease.getLandlordId()),
                Enums.NotificationType.DISPUTE_UPDATE, resolved.getId().toString());
        notifications.notifyAdmins(Enums.NotificationType.DISPUTE_UPDATE, hold.getId().toString());
        return resolved;
    }

    @Override
    @Transactional(readOnly = true)
    public Dispute get(Actor actor, UUID disputeId) {
        Dispute dispute = find(disputeId);
        if (!actor.has(Enums.Role.ADMIN)) {
            lookup.requirePartyTo(actor, leaseOf(escrow.get(dispute.getDepositHoldId())));
        }
        return dispute;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> openDisputes(Actor admin) {
        lookup.admin(admin);
        return disputes.findByStatus(Enums.DisputeStatus.OPEN);
    }

    private Dispute find(UUID id) {
        return disputes.findById(id).orElseThrow(() -> new NotFoundException("Dispute not found"));
    }

    private Lease leaseOf(DepositHold hold) {
        return leases.findById(hold.getLeaseId()).orElseThrow(() -> new NotFoundException("Lease not found"));
    }
}
