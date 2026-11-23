package com.hlala.domain;

import jakarta.persistence.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report")
public class Report {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID reportedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ReportReason reason;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "report_attachment", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "attachment_uri")
    private List<URI> attachmentRefs;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Report() {
    }

    public Report(UUID id, UUID reportedByUserId, Enums.ReportReason reason, String description,
                  List<URI> attachmentRefs, Instant createdAt) {
        this.id = id;
        this.reportedByUserId = reportedByUserId;
        this.reason = reason;
        this.description = description;
        this.attachmentRefs = attachmentRefs;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getReportedByUserId() {
        return reportedByUserId;
    }

    public Enums.ReportReason getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public List<URI> getAttachmentRefs() {
        return attachmentRefs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
