package com.hlala.schemas;

import com.hlala.domain.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Meter, repairs, reviews and notifications. */
public final class LivingSchemas {

    private LivingSchemas() {
    }

    public record TopUpRequest(@NotNull @DecimalMin("1") BigDecimal amount, @NotNull Enums.SplitMode mode,
                               Map<UUID, Integer> occupantsByLease) {
    }

    public record MeterSplitDto(UUID leaseId, BigDecimal share, Enums.SplitMode mode) {
        public static MeterSplitDto of(MeterSplit s) {
            return new MeterSplitDto(s.getLeaseId(), s.getShareAmount(), s.getMode());
        }
    }

    public record TopUpDto(UUID readingId, BigDecimal amount, Instant recordedAt, List<MeterSplitDto> splits) {
    }

    public record RepairRequest(@NotBlank String description) {
    }

    public record AdvanceRepairRequest(@NotNull Enums.RepairStatus next) {
    }

    public record RepairDto(UUID id, UUID leaseId, String description, Enums.RepairStatus status, Instant reportedAt) {
        public static RepairDto of(RepairTicket t) {
            return new RepairDto(t.getId(), t.getLeaseId(), t.getDescription(), t.getStatus(), t.getReportedAt());
        }
    }

    public record ReviewRequest(@Min(1) @Max(5) int stars, Set<String> tags, String comment) {
    }

    public record ReviewDto(UUID id, UUID authorId, UUID aboutUserId, int stars, Set<String> tags, String comment,
                            boolean sealed, Instant postedAt) {
        public static ReviewDto of(Review r) {
            return new ReviewDto(r.getId(), r.getAuthorId(), r.getAboutUserId(), r.getStars(), r.getTags(),
                    r.getComment(), r.isSealed(), r.getPostedAt());
        }
    }

    public record NotificationDto(UUID id, Enums.NotificationType type, String payloadRef, boolean read,
                                  Instant createdAt) {
        public static NotificationDto of(Notification n) {
            return new NotificationDto(n.getId(), n.getType(), n.getPayloadRef(), n.isRead(), n.getCreatedAt());
        }
    }
}
