package com.example.backend.exception;

public class AIException extends RuntimeException {
    public AIException(String message, Exception e) {
        super(message);
    }

    public AIException(String message) {
        super(message);
    }
}
