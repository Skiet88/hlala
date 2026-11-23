-- Hlala baseline schema.
--
-- Started from Hibernate's generated DDL for the entities, then tidied by hand:
-- foreign keys between tables, uniqueness rules the services already assume,
-- and CHECK rules that repeat the factory validation so bad rows cannot get in
-- even if a code path skips a factory. Hibernate runs with ddl-auto=validate,
-- so this file (and the ones after it) is the only thing that shapes the schema.
--
-- Id conventions:
--   *_user_id, author_id, about_user_id  -> app_user.id
--   tenant_id   -> tenant_profile.id
--   landlord_id -> landlord_profile.id
--   checker_id  -> checker_profile.id

-- ------------------------------------------------------------------ people

create table app_user (
    id              uuid         primary key,
    phone           varchar(32)  not null,
    name            varchar(255) not null,
    phone_verified  boolean      not null,
    created_at      timestamptz  not null,
    constraint uk_app_user_phone unique (phone)
);

create table tenant_profile (
    id            uuid        primary key,
    user_id       uuid        not null,
    onboarded_at  timestamptz not null,
    constraint uk_tenant_profile_user unique (user_id),
    constraint fk_tenant_profile_user foreign key (user_id) references app_user (id)
);

create table landlord_profile (
    id            uuid        primary key,
    user_id       uuid        not null,
    onboarded_at  timestamptz not null,
    constraint uk_landlord_profile_user unique (user_id),
    constraint fk_landlord_profile_user foreign key (user_id) references app_user (id)
);

create table checker_profile (
    id            uuid        primary key,
    user_id       uuid        not null,
    onboarded_at  timestamptz not null,
    constraint uk_checker_profile_user unique (user_id),
    constraint fk_checker_profile_user foreign key (user_id) references app_user (id)
);

create table admin_profile (
    id            uuid        primary key,
    user_id       uuid        not null,
    onboarded_at  timestamptz not null,
    constraint uk_admin_profile_user unique (user_id),
    constraint fk_admin_profile_user foreign key (user_id) references app_user (id)
);

-- --------------------------------------------------------- rooms, listings

create table room (
    id              uuid         primary key,
    landlord_id     uuid         not null,
    area            varchar(255) not null,
    landmark_note   varchar(255),
    latitude        double precision,
    longitude       double precision,
    type            varchar(32)  not null,
    occupancy_mode  varchar(32)  not null,
    created_at      timestamptz  not null,
    constraint fk_room_landlord foreign key (landlord_id) references landlord_profile (id),
    constraint ck_room_type check (type in ('BACKYARD_ROOM','GRANNY_FLAT','SHARED_ROOM','FULL_UNIT')),
    constraint ck_room_occupancy_mode check (occupancy_mode in ('SOLE','SHARED'))
);

create table room_amenity (
    room_id  uuid        not null,
    amenity  varchar(32) not null,
    constraint pk_room_amenity primary key (room_id, amenity),
    constraint fk_room_amenity_room foreign key (room_id) references room (id),
    constraint ck_room_amenity check (amenity in ('PRIVATE_ENTRANCE','PRIVATE_TOILET','SHARED_TOILET',
        'WATER_INSIDE','ELECTRICITY_PREPAID','ELECTRICITY_INCLUDED','WIFI','SECURE_YARD','PARKING','FURNISHED'))
);

create table listing (
    id                   uuid          primary key,
    room_id              uuid          not null,
    rent_amount          numeric(12,2) not null,
    deposit_amount       numeric(12,2) not null,
    status               varchar(32)   not null,
    verification_status  varchar(32)   not null,
    created_at           timestamptz   not null,
    published_at         timestamptz,
    constraint fk_listing_room foreign key (room_id) references room (id),
    constraint ck_listing_rent check (rent_amount > 0),
    constraint ck_listing_deposit check (deposit_amount > 0),
    constraint ck_listing_status check (status in ('DRAFT','LIVE','LET','ARCHIVED')),
    constraint ck_listing_verification check (verification_status in ('UNVERIFIED','PENDING_CHECK','VERIFIED'))
);

create table saved_listing (
    id          uuid        primary key,
    tenant_id   uuid        not null,
    listing_id  uuid        not null,
    saved_at    timestamptz not null,
    constraint uk_saved_listing unique (tenant_id, listing_id),
    constraint fk_saved_listing_tenant foreign key (tenant_id) references tenant_profile (id),
    constraint fk_saved_listing_listing foreign key (listing_id) references listing (id)
);

create table viewing (
    id             uuid        primary key,
    listing_id     uuid        not null,
    tenant_id      uuid        not null,
    scheduled_for  timestamptz not null,
    status         varchar(32) not null,
    constraint fk_viewing_listing foreign key (listing_id) references listing (id),
    constraint fk_viewing_tenant foreign key (tenant_id) references tenant_profile (id),
    constraint ck_viewing_status check (status in ('BOOKED','COMPLETED','NO_SHOW','CANCELLED'))
);

-- ------------------------------------------------------------- room checks

create table room_check (
    id            uuid        primary key,
    listing_id    uuid        not null,
    checker_id    uuid        not null,
    passed        boolean     not null,
    latitude      double precision,
    longitude     double precision,
    completed_at  timestamptz not null,
    constraint fk_room_check_listing foreign key (listing_id) references listing (id),
    constraint fk_room_check_checker foreign key (checker_id) references checker_profile (id)
);

create table room_check_result (
    room_check_id  uuid         not null,
    position       integer      not null,
    item           varchar(255),
    passed         boolean,
    note           varchar(255),
    constraint pk_room_check_result primary key (room_check_id, position),
    constraint fk_room_check_result_check foreign key (room_check_id) references room_check (id)
);

create table room_check_photo (
    room_check_id  uuid          not null,
    position       integer       not null,
    photo_uri      varchar(2048),
    constraint pk_room_check_photo primary key (room_check_id, position),
    constraint fk_room_check_photo_check foreign key (room_check_id) references room_check (id)
);

-- ---------------------------------------------------- applications, leases

create table application (
    id                  uuid        primary key,
    listing_id          uuid        not null,
    tenant_id           uuid        not null,
    status              varchar(32) not null,
    id_verified         boolean     not null,
    income_verified     boolean     not null,
    references_checked  boolean     not null,
    applied_at          timestamptz not null,
    constraint fk_application_listing foreign key (listing_id) references listing (id),
    constraint fk_application_tenant foreign key (tenant_id) references tenant_profile (id),
    constraint ck_application_status check (status in ('PENDING','ACCEPTED','DECLINED','WITHDRAWN'))
);

-- A tenant can have only one pending application per listing.
create unique index uk_application_pending
    on application (tenant_id, listing_id) where status = 'PENDING';

create table lease (
    id                  uuid          primary key,
    room_id             uuid          not null,
    tenant_id           uuid          not null,
    landlord_id         uuid          not null,
    monthly_rent        numeric(12,2) not null,
    start_date          date          not null,
    end_date            date,
    language            varchar(8)    not null,
    tenant_signature    varchar(32)   not null,
    landlord_signature  varchar(32)   not null,
    constraint fk_lease_room foreign key (room_id) references room (id),
    constraint fk_lease_tenant foreign key (tenant_id) references tenant_profile (id),
    constraint fk_lease_landlord foreign key (landlord_id) references landlord_profile (id),
    constraint ck_lease_rent check (monthly_rent > 0),
    constraint ck_lease_dates check (end_date is null or end_date >= start_date),
    constraint ck_lease_language check (language in ('EN','XH')),
    constraint ck_lease_tenant_signature check (tenant_signature in ('UNSIGNED','SIGNED')),
    constraint ck_lease_landlord_signature check (landlord_signature in ('UNSIGNED','SIGNED'))
);

-- ------------------------------------------------------------------ escrow

create table deposit_hold (
    id                     uuid          primary key,
    lease_id               uuid          not null,
    amount                 numeric(12,2) not null,
    state                  varchar(32)   not null,
    handover_confirmed_at  timestamptz,
    created_at             timestamptz   not null,
    -- One hold per lease. The lease does not point back (see CLAUDE.md).
    constraint uk_deposit_hold_lease unique (lease_id),
    constraint fk_deposit_hold_lease foreign key (lease_id) references lease (id),
    constraint ck_deposit_hold_amount check (amount > 0),
    constraint ck_deposit_hold_state check (state in
        ('PENDING','HELD','ACTIVE','DISPUTED','RELEASED','REFUNDED','PARTIALLY_SETTLED'))
);

-- Append only audit log. Nothing in the application updates or deletes rows here.
create table deposit_hold_event (
    id                     uuid         primary key,
    deposit_hold_id        uuid         not null,
    from_state             varchar(32)  not null,
    to_state               varchar(32)  not null,
    triggered_by_user_id   uuid         not null,
    reason                 varchar(255),
    occurred_at            timestamptz  not null,
    constraint fk_deposit_hold_event_hold foreign key (deposit_hold_id) references deposit_hold (id),
    constraint fk_deposit_hold_event_user foreign key (triggered_by_user_id) references app_user (id),
    constraint ck_deposit_hold_event_from check (from_state in
        ('PENDING','HELD','ACTIVE','DISPUTED','RELEASED','REFUNDED','PARTIALLY_SETTLED')),
    constraint ck_deposit_hold_event_to check (to_state in
        ('PENDING','HELD','ACTIVE','DISPUTED','RELEASED','REFUNDED','PARTIALLY_SETTLED'))
);

create table admin_approval (
    id                uuid        primary key,
    deposit_hold_id   uuid        not null,
    admin_profile_id  uuid        not null,
    approved_at       timestamptz not null,
    constraint fk_admin_approval_hold foreign key (deposit_hold_id) references deposit_hold (id),
    constraint fk_admin_approval_admin foreign key (admin_profile_id) references admin_profile (id)
);

create table dispute (
    id                 uuid        primary key,
    deposit_hold_id    uuid        not null,
    raised_by_user_id  uuid        not null,
    status             varchar(32) not null,
    resolution         varchar(32),
    opened_at          timestamptz not null,
    resolved_at        timestamptz,
    constraint fk_dispute_hold foreign key (deposit_hold_id) references deposit_hold (id),
    constraint fk_dispute_raised_by foreign key (raised_by_user_id) references app_user (id),
    constraint ck_dispute_status check (status in ('OPEN','RESOLVED')),
    constraint ck_dispute_resolution check (resolution in ('LANDLORD','TENANT','SPLIT'))
);

-- At most one open dispute per hold.
create unique index uk_dispute_open_per_hold
    on dispute (deposit_hold_id) where status = 'OPEN';

create table dispute_evidence (
    dispute_id    uuid          not null,
    evidence_uri  varchar(2048),
    constraint fk_dispute_evidence_dispute foreign key (dispute_id) references dispute (id)
);

-- ------------------------------------------------------------- rent, meter

create table rent_payment (
    id                   uuid          primary key,
    lease_id             uuid          not null,
    amount               numeric(12,2) not null,
    method               varchar(32)   not null,
    covered_period       varchar(7)    not null,
    recorded_at          timestamptz   not null,
    recorded_by_user_id  uuid          not null,
    constraint fk_rent_payment_lease foreign key (lease_id) references lease (id),
    constraint fk_rent_payment_recorded_by foreign key (recorded_by_user_id) references app_user (id),
    constraint ck_rent_payment_amount check (amount > 0),
    constraint ck_rent_payment_method check (method in ('CASH','EFT','CARD')),
    constraint ck_rent_payment_period check (covered_period ~ '^[0-9]{4}-[0-9]{2}$')
);

create table meter_reading (
    id             uuid          primary key,
    room_id        uuid          not null,
    top_up_amount  numeric(12,2) not null,
    recorded_at    timestamptz   not null,
    constraint fk_meter_reading_room foreign key (room_id) references room (id),
    constraint ck_meter_reading_amount check (top_up_amount > 0)
);

create table meter_split (
    id                uuid          primary key,
    meter_reading_id  uuid          not null,
    lease_id          uuid          not null,
    share_amount      numeric(12,2) not null,
    mode              varchar(32)   not null,
    constraint fk_meter_split_reading foreign key (meter_reading_id) references meter_reading (id),
    constraint fk_meter_split_lease foreign key (lease_id) references lease (id),
    constraint ck_meter_split_amount check (share_amount >= 0),
    constraint ck_meter_split_mode check (mode in ('EQUAL_PER_ROOM','PER_PERSON'))
);

-- ------------------------------------------------------ living, trust, misc

create table repair_ticket (
    id           uuid          primary key,
    lease_id     uuid          not null,
    description  varchar(1000) not null,
    status       varchar(32)   not null,
    reported_at  timestamptz   not null,
    constraint fk_repair_ticket_lease foreign key (lease_id) references lease (id),
    constraint ck_repair_ticket_status check (status in ('REPORTED','SEEN','BOOKED','CONFIRMED_FIXED'))
);

create table review (
    id             uuid          primary key,
    lease_id       uuid          not null,
    author_id      uuid          not null,
    about_user_id  uuid          not null,
    stars          integer       not null,
    comment        varchar(1000),
    sealed         boolean       not null,
    posted_at      timestamptz   not null,
    -- One review per side per tenancy.
    constraint uk_review_lease_author unique (lease_id, author_id),
    constraint fk_review_lease foreign key (lease_id) references lease (id),
    constraint fk_review_author foreign key (author_id) references app_user (id),
    constraint fk_review_about foreign key (about_user_id) references app_user (id),
    constraint ck_review_stars check (stars between 1 and 5),
    constraint ck_review_not_self check (author_id <> about_user_id)
);

create table review_tag (
    review_id  uuid         not null,
    tag        varchar(255) not null,
    constraint pk_review_tag primary key (review_id, tag),
    constraint fk_review_tag_review foreign key (review_id) references review (id)
);

create table report (
    id                   uuid          primary key,
    reported_by_user_id  uuid          not null,
    reason               varchar(32)   not null,
    description          varchar(2000),
    created_at           timestamptz   not null,
    constraint fk_report_reported_by foreign key (reported_by_user_id) references app_user (id),
    constraint ck_report_reason check (reason in
        ('SUSPECTED_SCAM','UNSAFE_CONDITIONS','HARASSMENT','PAYMENT_ISSUE','OTHER'))
);

create table report_attachment (
    report_id       uuid          not null,
    attachment_uri  varchar(2048),
    constraint fk_report_attachment_report foreign key (report_id) references report (id)
);

create table notification (
    id           uuid         primary key,
    user_id      uuid         not null,
    type         varchar(32)  not null,
    payload_ref  varchar(255),
    read         boolean      not null,
    created_at   timestamptz  not null,
    constraint fk_notification_user foreign key (user_id) references app_user (id),
    constraint ck_notification_type check (type in ('VIEWING_CONFIRMED','APPLICATION_DECISION',
        'PAYMENT_DUE','PAYMENT_RECEIVED','DEPOSIT_RELEASED','DISPUTE_UPDATE','REPAIR_UPDATE',
        'REVIEW_UNLOCKED','GENERAL'))
);
