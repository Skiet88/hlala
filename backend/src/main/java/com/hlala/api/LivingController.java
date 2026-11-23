package com.hlala.api;

import com.hlala.config.CurrentActor;
import com.hlala.schemas.LivingSchemas.*;
import com.hlala.service.Actor;
import com.hlala.service.LivingService;
import com.hlala.service.MeterService;
import com.hlala.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Meter splitting, repairs, reviews and notifications. */
@RestController
public class LivingController {

    private final MeterService meter;
    private final LivingService living;
    private final NotificationService notifications;

    public LivingController(MeterService meter, LivingService living, NotificationService notifications) {
        this.meter = meter;
        this.living = living;
        this.notifications = notifications;
    }

    @PostMapping("/rooms/{roomId}/meter-top-ups")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    @ResponseStatus(HttpStatus.CREATED)
    public TopUpDto topUp(@CurrentActor Actor actor, @PathVariable UUID roomId, @Valid @RequestBody TopUpRequest req) {
        MeterService.TopUpResult r = meter.recordTopUp(actor, roomId, req.amount(), req.mode(), req.occupantsByLease());
        return new TopUpDto(r.reading().getId(), r.reading().getTopUpAmount(), r.reading().getRecordedAt(),
                r.splits().stream().map(MeterSplitDto::of).toList());
    }

    @GetMapping("/leases/{leaseId}/meter-splits")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public List<MeterSplitDto> splits(@CurrentActor Actor actor, @PathVariable UUID leaseId) {
        return meter.splitsForLease(actor, leaseId).stream().map(MeterSplitDto::of).toList();
    }

    @PostMapping("/leases/{leaseId}/repairs")
    @PreAuthorize("hasRole('TENANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public RepairDto reportRepair(@CurrentActor Actor actor, @PathVariable UUID leaseId,
                                  @Valid @RequestBody RepairRequest req) {
        return RepairDto.of(living.reportRepair(actor, leaseId, req.description()));
    }

    @GetMapping("/leases/{leaseId}/repairs")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public List<RepairDto> repairs(@CurrentActor Actor actor, @PathVariable UUID leaseId) {
        return living.repairs(actor, leaseId).stream().map(RepairDto::of).toList();
    }

    @PostMapping("/repairs/{id}/advance")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public RepairDto advance(@CurrentActor Actor actor, @PathVariable UUID id,
                             @Valid @RequestBody AdvanceRepairRequest req) {
        return RepairDto.of(living.advanceRepair(actor, id, req.next()));
    }

    @PostMapping("/leases/{leaseId}/reviews")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto review(@CurrentActor Actor actor, @PathVariable UUID leaseId,
                            @Valid @RequestBody ReviewRequest req) {
        return ReviewDto.of(living.postReview(actor, leaseId, req.stars(), req.tags(), req.comment()));
    }

    @GetMapping("/leases/{leaseId}/reviews")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public List<ReviewDto> reviews(@CurrentActor Actor actor, @PathVariable UUID leaseId) {
        return living.reviews(actor, leaseId).stream().map(ReviewDto::of).toList();
    }

    @GetMapping("/notifications")
    public List<NotificationDto> notifications(@CurrentActor Actor actor) {
        return notifications.forUser(actor).stream().map(NotificationDto::of).toList();
    }

    @PostMapping("/notifications/{id}/read")
    public NotificationDto markRead(@CurrentActor Actor actor, @PathVariable UUID id) {
        return NotificationDto.of(notifications.markRead(actor, id));
    }

    @PostMapping("/notifications/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@CurrentActor Actor actor) {
        notifications.markAllRead(actor);
    }
}
