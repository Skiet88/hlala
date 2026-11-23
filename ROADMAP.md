# Hlala roadmap

The plan from the current state to launch. CLAUDE.md has the context; this file
has the order of work.

## How to use this file (for Claude)

1. At the start of a session, find the first phase whose status is not DONE.
   That is the current phase. If the owner asks for something else, do that,
   but say which phase it belongs to.
2. Before building anything in a phase, give the owner a short plan for it and
   wait for him to say go (his standing rule). Then finish it fully.
3. Items marked **OWNER DECISION** are his to make. Present the options with a
   recommendation and stop; never pick for him.
4. At the end of every session: tick finished items, set phase status, add new
   decisions or discovered work to the right phase, update the "Last updated"
   line, then commit and push as the owner (see CLAUDE.md).
5. If work reveals the order below is wrong, say so and propose the change. Do
   not silently reorder.

Last updated: 2026-11-23, end of the planning chat. Backend written, never built.

## Why this order

Make it build before changing it. Put a real database and migrations in place
before any real data exists. Real sign in before anyone outside can touch it.
File storage before checks, because the trust badge depends on photos. Payments
come late because they carry the most risk and need business decisions first.
The frontend can start once sign in exists, in parallel with phases 3 to 5.

---

## Phase 0: Make it build — status: NEXT

- [ ] `cd backend && mvn test` passes. Fix wiring or mapping issues; do not
      redesign. If a fix would change a design decision in CLAUDE.md, ask first.
- [ ] App starts with `mvn spring-boot:run`; seeded admin ids print in the log.
- [ ] Walk the journey by hand once (or via `TenancyFlowIntegrationTest`).
- [ ] GitHub Actions workflow running `mvn test` on every push and PR.
- [ ] Open a PR from `feature/backend-spring-boot` to `main`; owner merges.

Done when: tests are green locally and in CI, and the branch is merged.

## Phase 1: Real database — status: TODO

- [ ] Flyway migrations for the current schema; switch local and prod away from
      `ddl-auto=update` (prod already uses `validate`).
- [ ] Docker Compose with Postgres for local work; keep H2 only for tests.
- [ ] Indexes for the hot queries (listings by status, holds by lease, events by hold).
- [ ] **OWNER DECISION:** hosting for the database and app (Render is already
      connected to his account; other options are fine).

Done when: the app runs on Postgres from migrations alone.

## Phase 2: Real sign in — status: TODO

- [ ] Phone number plus one time PIN by SMS, behind an `OtpSender` interface.
- [ ] Short lived access tokens plus refresh tokens; replace the dev header in
      every profile except `local`.
- [ ] Rate limit OTP requests and attempts per phone and per IP; lock after
      repeated failures. SIM swap fraud is real here, so treat this as security work.
- [ ] Mark `User.phoneVerified` on success.
- [ ] **OWNER DECISION:** SMS provider (cost per message matters at this scale).

Done when: no protected endpoint can be reached without a verified phone, and
the dev header is impossible outside `local`.

## Phase 3: Files and the checker workflow — status: TODO

- [ ] Object storage behind a `FileStore` interface (S3 compatible); uploads via
      short lived signed URLs; size and type limits.
- [ ] Room photos, checker photos, voice notes, dispute evidence all go through it.
- [ ] Checker photos must be taken in the app: record capture time and location
      server side and reject gallery uploads, so a checker cannot fake a visit.
- [ ] Strip location data from photos shown publicly.
- [ ] Checker job assignment (today checkers only see a queue).
- [ ] **OWNER DECISION:** how checkers are paid; this is the `[PAY PER CHECK]`
      placeholder. Do not invent a number.

Done when: a listing can be verified end to end with real photos.

## Phase 4: Finish the tenancy rules — status: TODO

Gaps known from the planning chat:
- [ ] **Split settlements need amounts.** `RESOLVED_SPLIT` / `PARTIALLY_SETTLED`
      records no amounts yet. The MoveOut screen shows itemised deductions (for
      example R350 for a window) that the tenant accepts or disputes. Model the
      deductions, and make the split carry the landlord and tenant amounts,
      which must add up to the hold.
- [ ] **Verification ticks have no flow.** Nothing sets `idVerified`,
      `incomeVerified` or `referencesChecked`. Build the verification step;
      keep the rule that landlords see ticks only, never documents.
- [ ] Move-in and move-out condition photos (the MoveIn and MoveOut screens).
- [ ] Lease signing details: a plain English and isiXhosa document and the audio
      "listen" option from the Lease screen.
- [ ] Wanted board (tenants post what they need; landlords respond).
- [ ] Viewing reschedule, cancel and no-show.

Done when: every screen in the prototype has the backend it needs, except
chat and payments.

## Phase 5: Notifications and chat — status: TODO

- [ ] Delivery channels behind the existing `NotificationService`: in app, SMS
      fallback (the Saved screen promises SMS alerts).
- [ ] Saved search alerts.
- [ ] In app chat between tenant and landlord, with `FraudSignalScorer` run on
      messages and the scam warning banner from the Chat screen.
- [ ] Replace the keyword fraud scorer with a better one, still behind the interface.
- [ ] **OWNER DECISION:** whether to use WhatsApp as a channel.

## Phase 6: Payments — status: TODO

- [ ] `PaymentGateway` interface; webhook replaces the admin
      `payment-confirmed` endpoint. Verify webhook signatures and make every
      handler idempotent; payment code must survive duplicate and replayed events.
- [ ] Payout to the landlord or refund to the tenant after a terminal hold
      state, recorded against the hold's audit log.
- [ ] Cash at a till, EFT, card, and "pay in two parts", as on the Pay screen.
- [ ] Reconciliation report for admins.
- [ ] **OWNER DECISION:** payment provider.
- [ ] **OWNER DECISION:** the platform fee; this is the `[YOUR FEE]` placeholder.
- [ ] **OWNER DECISION, get advice:** holding other people's deposits may need a
      licensed partner (for example a bank escrow or trust account). Raise this
      before going live with real money; do not assume either way.

## Phase 7: Frontend — status: TODO (can start after Phase 2)

- [ ] Build from the 30 screen prototype (link in CLAUDE.md), same palette and fonts.
- [ ] Works on low end Android phones and is light on mobile data.
- [ ] English and isiXhosa.
- [ ] Admin back office as a web app (the Admin screen).
- [ ] **OWNER DECISION:** technology, for example a PWA, React Native or
      Flutter. Recommend with reasons; he chooses.

## Phase 8: Launch readiness — status: TODO

- [ ] POPIA: consent, data export and deletion requests (the Admin screen has a
      privacy requests section), retention rules.
- [ ] Security review: authorisation tests for every endpoint, dependency
      scanning, secrets management, a penetration test pass.
- [ ] Logging, metrics and alerts; backups with a tested restore.
- [ ] Deploy pipeline and a staging environment.
- [ ] Small pilot in one area before a wider launch.
