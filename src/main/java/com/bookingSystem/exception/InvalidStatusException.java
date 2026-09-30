package com.bookingSystem.exception;

public class InvalidStatusException extends RuntimeException
{
    public InvalidStatusException(String message) {
        super(message);
    }
}
