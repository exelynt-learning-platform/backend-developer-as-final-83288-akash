package com.bookingSystem.exception;

public class UserIdConflictException extends RuntimeException {
    public UserIdConflictException(String message) {
        super(message);
    }
}
