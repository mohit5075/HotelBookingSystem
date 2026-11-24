package com.hotel.booking.exception;

public class RoomNotAvailableException extends ConflictException {

    public RoomNotAvailableException(String message) {
        super(message);
    }
}
