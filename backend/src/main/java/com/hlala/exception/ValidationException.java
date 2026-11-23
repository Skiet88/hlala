package com.hlala.exception;

/** Thrown by factories when input fails a domain rule. Maps to HTTP 400. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
