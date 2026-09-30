package com.bookingSystem.exception;

public class ReservationDoesNotExistException extends RuntimeException {
    public ReservationDoesNotExistException(String message) {
        super(message);
    }
}
