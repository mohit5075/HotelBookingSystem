package com.hotel.booking.exception;

/** Base type for requests that are valid but conflict with the current state (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
