package com.hotel.booking.dto;

import com.hotel.booking.model.DateRange;
import com.hotel.booking.service.BookingCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateBookingRequest(
        @NotBlank String propertyId,
        @NotBlank String roomTypeId,
        @NotBlank String guestName,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @Min(1) int guests) {

    public BookingCommand toNewBooking() {
        return new BookingCommand(propertyId, roomTypeId, guestName, new DateRange(checkIn, checkOut), guests);
    }
}
