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
- Pushing is allowed to `feature/backend-spring-boot` only, and only when he
  confirms. Finish a piece of work, show him what changed, ask whether to push,
  and push after he says yes. The same goes for commits and PRs: a "go" for a
  task is not a go to push or open a PR.
- If a plan question is unanswered, ask again; never treat "continue" as a yes
  to it.
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
- Flyway owns the schema (`backend/src/main/resources/db/migration`). Hibernate
  only validates, in every profile. Never edit an applied migration; add a new
  `V<n>__...sql`. Id columns: `tenant_id`, `landlord_id`, `checker_id` point at
  profile rows; `*_user_id`, `author_id`, `about_user_id` point at `app_user`.
- The database repeats key rules as constraints: one hold per lease, one open
  dispute per hold, one pending application per tenant per listing, one review
  per side per lease. Violations return 409.
- Row level security is on for every table, with no policies (V4, plus
  `afterMigrate.sql` for Flyway's history table), so Supabase's Data API and
  its public anon key cannot reach any data. The backend owns the tables and is
  not affected. Every new table must enable it in its migration;
  `everyTableHasRowLevelSecurity` fails CI otherwise. Also turn the Data API
  off in the Supabase dashboard when the project is connected.
- Optimistic locking: `DepositHold`, `Dispute` and `Lease` have `@Version`, and
  their `withX()` copies carry the version, so a write from a stale read fails.
  `approve()` loads the hold with `OPTIMISTIC_FORCE_INCREMENT` because an
  approval decides about the hold without changing it. A conflict returns 409
  and the client retries.

## Current state

- Builds with Maven. All 40 tests pass (JUnit, Spring context, H2,
  `SecurityConfig`), and the full journey was walked by hand against the running
  app. CI runs `mvn test` on JDK 17 for every push and PR
  (`.github/workflows/backend-ci.yml`).
- Until the backend PR is merged, keep pushing backend work to
  `feature/backend-spring-boot` (owner's instruction).
- The backend is merged into `main` (PR #2). Backend work still goes on
  `feature/backend-spring-boot`, kept in step with `main`, with a PR per phase.
- Runs on Postgres from Flyway migrations alone (V1 to V4), checked against
  Postgres 16. 43 tests pass in CI, including a Testcontainers Postgres
  integration test and concurrency tests. Not yet pointed at the Supabase
  project.
- Testcontainers is pinned to 1.21.4 in the pom: the version Boot 3.3 brings
  cannot talk to Docker 29 or newer.
- **What comes next:** see ROADMAP.md. Phase 1 is in review (PR open); the
  hosting decision is still the owner's. Phase 2 (real sign in) follows.

## Running locally

Nothing runs on the owner's machine. The database is Supabase Postgres (owner's
decision), chosen so deployment is easy later.

- Tests run in GitHub Actions. The Spring test starts a throwaway Postgres with
  Testcontainers there, so CI needs Docker but the owner never does.
- The app connects to Supabase through `DATABASE_URL`, `DATABASE_USERNAME` and
  `DATABASE_PASSWORD` (session pooler, port 5432, `sslmode=require`), and
  Flyway applies the migrations on startup.
- In the `local` profile it seeds two admins and one checker the first time and
  prints their ids. Send a user id in the `X-Dev-User-Id` header
to act as that user (`DevHeaderAuthenticationFilter`, local only). In `prod`
nothing authenticates yet, so protected endpoints return 401 by design.
`TenancyFlowIntegrationTest` shows the full API journey.

## Frontend: `frontend/` (PWA: React, TypeScript, Vite)

Owner's decisions: a PWA, a mock API until real sign in, English only for now.

- Screens only talk to the `ApiClient` interface (`src/api/ApiClient.ts`).
  `MockApiClient` runs everything on sample data from the prototype;
  `HttpApiClient` calls the backend and today only serves public listing
  browsing, failing every other call with `NotAvailableYetError`. Pick with
  `VITE_API_MODE=mock|http` (see `.env.example`).
- Business rules the UI shows live as pure functions in `src/domain` (money,
  meter split, move out settlement, deposit state words, filters), tested.
- Design tokens and patterns: `src/styles/app.css`, from the prototype. Shared
  pieces in `src/components`. Screens by group in `src/screens`.
- `[YOUR FEE]` and `[PAY PER CHECK]` stay visible placeholders.
- Routes are not a security boundary; the backend enforces access.
- The service worker (`public/sw.js`) caches only the app shell and hashed
  assets, never API responses.
- `/screens` lists every screen, grouped like the prototype canvas.
- Run: `cd frontend && npm ci && npm test && npm run build`. Nothing needs to
  run on the owner's machine; CI does it (`.github/workflows/frontend-ci.yml`).

## Not built yet

Tracked phase by phase in ROADMAP.md, including known gaps and the decisions
that belong to the owner.

## End of every session

Update ROADMAP.md (ticks, phase status, new decisions, "Last updated") and this
file if any decision or the current state changed. Then commit and push both as
the owner. This is how the next session knows what happened.
