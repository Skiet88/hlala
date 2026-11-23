# Hlala: project context

Read this first, then ROADMAP.md. This file carries over everything decided in
the planning chat where this project was designed, so you do not need to ask the
owner to re-explain it. ROADMAP.md says what phase the project is in and what
comes next, so you can lead the work forward without being told the next step.

## Owner and how to work with him

- Owner: Mlungisi Leboghang Mbuyazi (Mlu), GitHub `Skiet88`, Cape Town.
  Postgraduate software engineering student (CPUT) with a security focus.
- Default to discussion and planning: explain and recommend first. Ask before
  writing or changing files, installing packages, running tests, committing,
  pushing or opening PRs, unless he has just told you to go ahead.
- Once he says go, finish the whole task without check-ins, and explain the
  reasoning behind decisions, not just the output.
- Commits must be authored by him, with no Claude co-author line:
  ```
  GIT_AUTHOR_NAME="Mlungisi Leboghang Mbuyazi" GIT_AUTHOR_EMAIL="mmbuyazi63@gmail.com" \
  GIT_COMMITTER_NAME="Mlungisi Leboghang Mbuyazi" GIT_COMMITTER_EMAIL="mmbuyazi63@gmail.com" \
  git commit -m "..."
  ```
  Do not change git config to do this.
- Writing he will publish (README text, PR descriptions): plain first person,
  no dashes, nothing that sounds machine written.

## What Hlala is

A rental platform for backyard rooms, granny flats and shared rooms in
Khayelitsha, Langa and nearby townships. The problem it solves is trust: rental
scams (fake listings, deposits taken for rooms that do not exist) are common.
The product answer is four trust mechanisms layered on normal browsing:

1. **Seen in person badge.** A Hlala checker visits the room, takes six photos
   in app, and the listing becomes VERIFIED. Unverified listings can still go
   live; the badge is earned, not a gate to publishing.
2. **Deposit held in escrow.** The tenant pays the deposit into a hold, not to
   the landlord. It only moves under strict rules (below).
3. **Rent record.** Payments are recorded, building a history for both sides.
4. **Sealed mutual reviews.** Each side's review stays hidden until both have
   posted, so neither writes in reaction to the other.

Not government related, by the owner's rule. Keep it private sector.

## The UI prototype

A 30 screen clickable design exists as a Claude Design artifact
("Kasi accommodation app: mock UI prototype"):
https://claude.ai/artifact/TKrChVc1ZenMqHzFM3M1Hy

Screens: Onboard, Explore (2 column tiles), Filters, MapView, Listing, Viewing,
Chat, Apply, Pay, MoveIn, MyHome; landlord ListPin, ListRoom, ListPhotos,
ListPublish, Landlord, Applicants, RentBook, Lease (English/isiXhosa); trust
Checker, Report, Dispute, Admin (web back office); living Saved,
Notifications, Wanted, Meter (prepaid split), Repair, MoveOut, Reviews.

Palette: background #F3F4EF, text #11150F, green #0B6B4A, amber #F5B82E.
Fonts: Inter (body), Space Grotesk (headings).

Two commercial placeholders are intentionally unresolved: `[YOUR FEE]` on Pay
and `[PAY PER CHECK]` on Checker. The pricing model is undecided; do not invent one.

## Backend: `backend/` (Spring Boot 3.3, Java 17)

### Architecture (owner's standard, keep to it)

SOLID, layered packages under `com.hlala`, each layer calling the one below
through interfaces:

`domain` entities (immutable: no setters, `withX()` methods return copies) ·
`factory` the only place entities are constructed, with validation ·
`repository` Spring Data JPA · `strategy` swappable rules · `screening`
room check rules · `llm` fraud scoring behind an interface (keyword stub now) ·
`service` business logic (interface + Impl) · `schemas` request/response DTOs ·
`api` thin controllers · `config` security and wiring · `exception` domain
exceptions and one global handler.

Patterns in use: Factory, Repository, Service, Strategy
(`MeterSplitStrategy`, `PayoutApprovalStrategy`), Facade
(`TenancyOnboardingFacade` for apply, pay, move in). Tests mirror the packages.

### The deposit state machine (most important code)

All state changes go through `EscrowService.transition`, which looks the move up
in `DepositHoldTransitionTable`. If an edge is not in the table it cannot happen.
Failures throw; nothing fails silently.

```
PENDING  -PAYMENT_CONFIRMED->        HELD
HELD     -MOVE_IN_CONFIRMED->        ACTIVE
HELD     -CANCELLED_BEFORE_MOVE_IN-> REFUNDED          (no admin approval)
ACTIVE   -DISPUTE_OPENED->           DISPUTED
ACTIVE   -RELEASE_AFTER_HANDOVER->   RELEASED          (handover + no open dispute + 2 admins)
DISPUTED -RESOLVED_FOR_LANDLORD->    RELEASED          (no open dispute + 2 admins)
DISPUTED -RESOLVED_FOR_TENANT->      REFUNDED          (no open dispute + 2 admins)
DISPUTED -RESOLVED_SPLIT->           PARTIALLY_SETTLED (no open dispute + 2 admins)
```

Payout rules:
- Two approvals from two *different* admin profiles (`TwoDistinctAdminsApprovalStrategy`).
- An admin who is the tenant or landlord on that lease cannot approve or resolve.
- Only approvals given after the hold's latest event count, so an approval
  cannot carry over to a decision the admin did not see.
- Resolving a dispute (`/disputes/{id}/resolve`) and approving the payout
  (`/deposit-holds/{id}/approvals`) are separate actions on purpose.
- Every change writes an append only `DepositHoldEvent`. That log is the admin
  Money trail; tenants and landlords only ever see state (`DepositHoldDto`), the
  audit view (`DepositHoldAuditDto`) is admin only and a separate class.

### Other decisions

- Roles: TENANT, LANDLORD (self chosen), CHECKER, ADMIN (granted by an admin only).
  Roles come from which profile rows exist; there is no role column.
- Access control has two layers: `@PreAuthorize` role checks on controllers, and
  ownership checks inside services via `ProfileLookup`, so ownership holds even
  if an annotation is forgotten.
- `Lease` does not store the hold id; `DepositHold.leaseId` points one way.
- `Room` vs `Listing`: a room is relisted after a tenant leaves without redoing
  its details. Shared rooms (`OccupancyMode.SHARED`) can have several leases;
  meter splits key off leases.
- Public listing views hide the exact GPS pin; a tenant gets it after booking a viewing.
- Application verification ticks start false and are never self asserted.
- Rent can only be recorded while the hold is ACTIVE or DISPUTED.

## Current state

- Branch `feature/backend-spring-boot` holds the backend. `main` has only a README.
- **Never built with Maven yet.** It was written in an environment that could not
  download dependencies. What was verified: every layer except `SecurityConfig`
  type checked against stand in annotations, and 60 end to end checks passed by
  wiring the real services over in memory repositories. The JUnit tests, the
  Spring context, H2 and `SecurityConfig` have never run.
- **What comes next:** see ROADMAP.md. The current phase is the first one not
  marked DONE (Phase 0, make it build, as of 2026-11-23).

## Running locally

```
cd backend
mvn test
mvn spring-boot:run
```

The `local` profile uses in memory H2 and seeds two admins and one checker; their
ids are printed in the startup log. Send a user id in the `X-Dev-User-Id` header
to act as that user (`DevHeaderAuthenticationFilter`, local only). In `prod`
nothing authenticates yet, so protected endpoints return 401 by design.
`TenancyFlowIntegrationTest` shows the full API journey.

## Not built yet

Tracked phase by phase in ROADMAP.md, including known gaps and the decisions
that belong to the owner.

## End of every session

Update ROADMAP.md (ticks, phase status, new decisions, "Last updated") and this
file if any decision or the current state changed. Then commit and push both as
the owner. This is how the next session knows what happened.
