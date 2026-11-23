package com.hlala.exception;

/** Maps to HTTP 403 - for ownership failures not already caught by role checks. */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
