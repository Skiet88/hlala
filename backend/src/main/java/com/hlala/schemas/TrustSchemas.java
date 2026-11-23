package com.hlala.schemas;

import com.hlala.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Reports, disputes and room checks. */
public final class TrustSchemas {

    private TrustSchemas() {
    }

    public record ReportRequest(@NotNull Enums.ReportReason reason, String description, List<URI> attachments) {
    }

    public record ReportDto(UUID id, Enums.ReportReason reason, Instant createdAt, boolean flaggedUrgent) {
    }

    public record OpenDisputeRequest(@NotNull UUID depositHoldId, List<URI> evidence) {
    }

    public record EvidenceRequest(@NotEmpty List<URI> evidence) {
    }

    public record ResolveDisputeRequest(@NotNull Enums.DisputeResolution resolution) {
    }

    public record DisputeDto(UUID id, UUID depositHoldId, Enums.DisputeStatus status,
                             Enums.DisputeResolution resolution, int evidenceCount, Instant openedAt,
                             Instant resolvedAt) {
        public static DisputeDto of(Dispute d) {
            return new DisputeDto(d.getId(), d.getDepositHoldId(), d.getStatus(), d.getResolution(),
                    d.getEvidenceRefs() == null ? 0 : d.getEvidenceRefs().size(), d.getOpenedAt(), d.getResolvedAt());
        }
    }

    public record ChecklistItemDto(@NotNull String item, boolean passed, String note) {
        public ChecklistItemResult toDomain() {
            return new ChecklistItemResult(item, passed, note);
        }

        public static ChecklistItemDto of(ChecklistItemResult r) {
            return new ChecklistItemDto(r.getItem(), r.isPassed(), r.getNote());
        }
    }

    public record SubmitRoomCheckRequest(@NotNull UUID listingId,
                                         @NotEmpty List<@Valid ChecklistItemDto> results,
                                         List<URI> photos,
                                         @NotNull ListingSchemas.PinDto checkedAt) {
    }

    public record RoomCheckDto(UUID id, UUID listingId, boolean passed, List<ChecklistItemDto> results,
                               int photoCount, Instant completedAt) {
        public static RoomCheckDto of(RoomCheck c) {
            return new RoomCheckDto(c.getId(), c.getListingId(), c.isPassed(),
                    c.getResults().stream().map(ChecklistItemDto::of).toList(),
                    c.getPhotoRefs() == null ? 0 : c.getPhotoRefs().size(), c.getCompletedAt());
        }
    }
}
