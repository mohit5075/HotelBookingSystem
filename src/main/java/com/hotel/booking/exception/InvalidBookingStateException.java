package com.hotel.booking.exception;

public class InvalidBookingStateException extends ConflictException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}
