package com.hlala.domain;

/**
 * Enums are grouped in one file for discoverability during early development.
 * Split into individual files once any of these grows real behavior.
 */
public final class Enums {

    private Enums() {
    }

    public enum RoomType {
        BACKYARD_ROOM, GRANNY_FLAT, SHARED_ROOM, FULL_UNIT
    }

    public enum Amenity {
        PRIVATE_ENTRANCE, PRIVATE_TOILET, SHARED_TOILET, WATER_INSIDE, ELECTRICITY_PREPAID,
        ELECTRICITY_INCLUDED, WIFI, SECURE_YARD, PARKING, FURNISHED
    }

    public enum OccupancyMode {
        SOLE, SHARED
    }

    public enum ListingStatus {
        DRAFT, LIVE, LET, ARCHIVED
    }

    public enum VerificationStatus {
        UNVERIFIED, PENDING_CHECK, VERIFIED
    }

    public enum ViewingStatus {
        BOOKED, COMPLETED, NO_SHOW, CANCELLED
    }

    public enum ApplicationStatus {
        PENDING, ACCEPTED, DECLINED, WITHDRAWN
    }

    public enum DepositHoldState {
        PENDING, HELD, ACTIVE, DISPUTED, RELEASED, REFUNDED, PARTIALLY_SETTLED
    }

    /** The events that can move a DepositHold. Each maps to exactly one legal (from, to) pair per state. */
    public enum DepositHoldTrigger {
        PAYMENT_CONFIRMED,
        MOVE_IN_CONFIRMED,
        CANCELLED_BEFORE_MOVE_IN,
        DISPUTE_OPENED,
        RELEASE_AFTER_HANDOVER,
        RESOLVED_FOR_LANDLORD,
        RESOLVED_FOR_TENANT,
        RESOLVED_SPLIT
    }

    public enum LeaseLanguage {
        EN, XH
    }

    public enum SignatureStatus {
        UNSIGNED, SIGNED
    }

    public enum PaymentMethod {
        CASH, EFT, CARD
    }

    public enum SplitMode {
        EQUAL_PER_ROOM, PER_PERSON
    }

    public enum ReportReason {
        SUSPECTED_SCAM, UNSAFE_CONDITIONS, HARASSMENT, PAYMENT_ISSUE, OTHER
    }

    public enum DisputeStatus {
        OPEN, RESOLVED
    }

    public enum DisputeResolution {
        LANDLORD, TENANT, SPLIT
    }

    public enum RepairStatus {
        REPORTED, SEEN, BOOKED, CONFIRMED_FIXED
    }

    public enum NotificationType {
        VIEWING_CONFIRMED, APPLICATION_DECISION, PAYMENT_DUE, PAYMENT_RECEIVED,
        DEPOSIT_RELEASED, DISPUTE_UPDATE, REPAIR_UPDATE, REVIEW_UNLOCKED, GENERAL
    }

    public enum Role {
        TENANT, LANDLORD, CHECKER, ADMIN
    }
}
