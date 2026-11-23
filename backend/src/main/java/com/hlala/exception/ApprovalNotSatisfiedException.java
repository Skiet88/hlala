package com.hlala.exception;

/**
 * Thrown when a payout transition is attempted without the two-distinct-admin
 * approval gate being satisfied. Maps to HTTP 409 - never silently ignored.
 */
public class ApprovalNotSatisfiedException extends RuntimeException {
    public ApprovalNotSatisfiedException(String message) {
        super(message);
    }
}
