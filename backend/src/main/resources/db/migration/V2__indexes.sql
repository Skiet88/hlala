-- Indexes for the queries the repositories run. Postgres does not index
-- foreign keys by itself, so every lookup column gets one unless a primary key,
-- unique constraint or unique index from V1 already covers it.

-- Browsing: public listing feed and the "verified only" filter.
create index ix_listing_status on listing (status);
create index ix_listing_verification_status on listing (verification_status);
create index ix_listing_room on listing (room_id);

-- Escrow. deposit_hold.lease_id is covered by uk_deposit_hold_lease.
create index ix_deposit_hold_event_hold_time on deposit_hold_event (deposit_hold_id, occurred_at);
create index ix_admin_approval_hold on admin_approval (deposit_hold_id);
create index ix_dispute_hold_status on dispute (deposit_hold_id, status);
create index ix_dispute_evidence_dispute on dispute_evidence (dispute_id);

-- Leases and what hangs off them.
create index ix_lease_tenant on lease (tenant_id);
create index ix_lease_landlord on lease (landlord_id);
create index ix_lease_room on lease (room_id);
create index ix_rent_payment_lease_period on rent_payment (lease_id, covered_period);
create index ix_repair_ticket_lease on repair_ticket (lease_id);
create index ix_meter_reading_room_time on meter_reading (room_id, recorded_at desc);
create index ix_meter_split_reading on meter_split (meter_reading_id);
create index ix_meter_split_lease on meter_split (lease_id);

-- Applications, viewings, rooms, checks.
create index ix_application_tenant on application (tenant_id);
create index ix_application_listing on application (listing_id);
create index ix_viewing_listing on viewing (listing_id);
create index ix_viewing_tenant on viewing (tenant_id);
create index ix_room_landlord on room (landlord_id);
create index ix_room_check_listing on room_check (listing_id);
create index ix_room_check_checker on room_check (checker_id);

-- Inbox: newest first per user.
create index ix_notification_user_time on notification (user_id, created_at desc);

-- Trust.
create index ix_report_reported_by on report (reported_by_user_id);
create index ix_report_attachment_report on report_attachment (report_id);
