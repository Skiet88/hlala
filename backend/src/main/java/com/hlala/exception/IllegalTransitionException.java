package com.hlala.exception;

/** Thrown when a requested DepositHold (or similar) state transition has no legal path. Maps to HTTP 409. */
public class IllegalTransitionException extends RuntimeException {
    public IllegalTransitionException(String message) {
        super(message);
    }
}
