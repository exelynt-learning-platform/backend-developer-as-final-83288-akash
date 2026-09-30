package com.bookingSystem.exception;

public class ReservationAlreadyConfirmedException extends RuntimeException {
    public ReservationAlreadyConfirmedException(String message) {
        super(message);
    }
}
