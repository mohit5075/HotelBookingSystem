package com.hotel.booking.model;

import com.hotel.booking.exception.InvalidRequestException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/** A stay: check-in night inclusive, check-out day exclusive. */
public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        if (checkIn == null || checkOut == null) {
            throw new InvalidRequestException("checkIn and checkOut are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidRequestException("checkOut must be after checkIn");
        }
    }

    public int nights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** Every night of the stay. */
    public Stream<LocalDate> dates() {
        return checkIn.datesUntil(checkOut);
    }
}
