package com.hlala.service;

import com.hlala.domain.Dispute;
import com.hlala.domain.Enums;
import com.hlala.domain.Report;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Reports and disputes: the trust-and-safety side. */
public interface TrustService {

    record ReportOutcome(Report report, boolean flaggedUrgent) {
    }

    ReportOutcome report(Actor actor, Enums.ReportReason reason, String description, List<URI> attachments);

    /** A party opens a dispute: the hold freezes (ACTIVE -> DISPUTED) in the same transaction. */
    Dispute openDispute(Actor party, UUID holdId, List<URI> evidence);

    Dispute addEvidence(Actor party, UUID disputeId, List<URI> evidence);

    /** Admin decides the outcome. Money still waits for the two-admin approval. */
    Dispute resolve(Actor admin, UUID disputeId, Enums.DisputeResolution resolution);

    Dispute get(Actor actor, UUID disputeId);

    List<Dispute> openDisputes(Actor admin);
}
