package com.civictrace.common.api;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
