package com.hlala.factory;

import com.hlala.domain.Enums;
import com.hlala.domain.Notification;
import com.hlala.domain.RepairTicket;
import com.hlala.domain.Report;
import com.hlala.domain.Review;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Records from living in a place: reports, repairs, reviews and notifications. */
@Component
public class CareFactory {

    static final int MAX_TEXT = 1000;
    static final int MAX_TAGS = 10;

    public Report report(UUID reportedByUserId, Enums.ReportReason reason, String description, List<URI> attachments) {
        if (reportedByUserId == null) {
            throw new ValidationException("A report must record who sent it");
        }
        if (reason == null) {
            throw new ValidationException("Pick a reason for the report");
        }
        boolean hasText = description != null && !description.isBlank();
        boolean hasAttachments = attachments != null && !attachments.isEmpty();
        if (!hasText && !hasAttachments) {
            throw new ValidationException("Describe the problem or attach a voice note or photo");
        }
        if (hasText && description.length() > 2000) {
            throw new ValidationException("Report description is too long");
        }
        return new Report(UUID.randomUUID(), reportedByUserId, reason, hasText ? description.trim() : null,
                hasAttachments ? List.copyOf(attachments) : List.of(), Instant.now());
    }

    public RepairTicket repair(UUID leaseId, String description) {
        if (leaseId == null) {
            throw new ValidationException("A repair must reference a lease");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("Describe what needs fixing");
        }
        if (description.length() > MAX_TEXT) {
            throw new ValidationException("Repair description is too long");
        }
        return new RepairTicket(UUID.randomUUID(), leaseId, description.trim(), Enums.RepairStatus.REPORTED, Instant.now());
    }

    public Review review(UUID leaseId, UUID authorId, UUID aboutUserId, int stars, Set<String> tags, String comment) {
        if (leaseId == null || authorId == null || aboutUserId == null) {
            throw new ValidationException("A review needs a lease, an author and who it is about");
        }
        if (authorId.equals(aboutUserId)) {
            throw new ValidationException("You cannot review yourself");
        }
        if (stars < 1 || stars > 5) {
            throw new ValidationException("Stars must be between 1 and 5");
        }
        if (tags != null && tags.size() > MAX_TAGS) {
            throw new ValidationException("Too many tags");
        }
        if (comment != null && comment.length() > MAX_TEXT) {
            throw new ValidationException("Review comment is too long");
        }
        return new Review(UUID.randomUUID(), leaseId, authorId, aboutUserId, stars,
                tags == null ? Set.of() : Set.copyOf(tags), comment, true, Instant.now());
    }

    public Notification notification(UUID userId, Enums.NotificationType type, String payloadRef) {
        if (userId == null || type == null) {
            throw new ValidationException("A notification needs a recipient and a type");
        }
        return new Notification(UUID.randomUUID(), userId, type, payloadRef, false, Instant.now());
    }
}
