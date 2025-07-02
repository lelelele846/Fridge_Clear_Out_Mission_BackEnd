package com.example.backend.exception;

public class ExpirationWarningException extends RuntimeException {
    public ExpirationWarningException(String message) {
        super(message);
    }
    public ExpirationWarningException(String message, Throwable cause) {
        super(message, cause);
    }
}
