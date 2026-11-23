package com.hlala.api;

import com.hlala.config.CurrentActor;
import com.hlala.schemas.TrustSchemas.*;
import com.hlala.service.Actor;
import com.hlala.service.RoomCheckService;
import com.hlala.service.TrustService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Reports, disputes (party side) and room checks (checker side). */
@RestController
public class TrustController {

    private final TrustService trust;
    private final RoomCheckService roomChecks;

    public TrustController(TrustService trust, RoomCheckService roomChecks) {
        this.trust = trust;
        this.roomChecks = roomChecks;
    }

    /** Any signed-in user can report. */
    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportDto report(@CurrentActor Actor actor, @Valid @RequestBody ReportRequest req) {
        TrustService.ReportOutcome out = trust.report(actor, req.reason(), req.description(), req.attachments());
        return new ReportDto(out.report().getId(), out.report().getReason(), out.report().getCreatedAt(),
                out.flaggedUrgent());
    }

    @PostMapping("/disputes")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    @ResponseStatus(HttpStatus.CREATED)
    public DisputeDto openDispute(@CurrentActor Actor actor, @Valid @RequestBody OpenDisputeRequest req) {
        return DisputeDto.of(trust.openDispute(actor, req.depositHoldId(), req.evidence()));
    }

    @PostMapping("/disputes/{id}/evidence")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD')")
    public DisputeDto addEvidence(@CurrentActor Actor actor, @PathVariable UUID id,
                                  @Valid @RequestBody EvidenceRequest req) {
        return DisputeDto.of(trust.addEvidence(actor, id, req.evidence()));
    }

    @GetMapping("/disputes/{id}")
    @PreAuthorize("hasAnyRole('TENANT','LANDLORD','ADMIN')")
    public DisputeDto dispute(@CurrentActor Actor actor, @PathVariable UUID id) {
        return DisputeDto.of(trust.get(actor, id));
    }

    // --- checker: the narrowest role, room checks only ------------------

    @GetMapping("/room-checks/queue")
    @PreAuthorize("hasRole('CHECKER')")
    public List<UUID> queue(@CurrentActor Actor actor) {
        return roomChecks.queue(actor).stream().map(l -> l.getId()).toList();
    }

    @PostMapping("/room-checks")
    @PreAuthorize("hasRole('CHECKER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomCheckDto submit(@CurrentActor Actor actor, @Valid @RequestBody SubmitRoomCheckRequest req) {
        return RoomCheckDto.of(roomChecks.submit(actor, req.listingId(),
                req.results().stream().map(ChecklistItemDto::toDomain).toList(),
                req.photos(), req.checkedAt().toGeoPoint()));
    }

    @GetMapping("/room-checks/mine")
    @PreAuthorize("hasRole('CHECKER')")
    public List<RoomCheckDto> mine(@CurrentActor Actor actor) {
        return roomChecks.mine(actor).stream().map(RoomCheckDto::of).toList();
    }
}
