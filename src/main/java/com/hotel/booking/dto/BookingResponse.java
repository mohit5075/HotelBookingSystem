package com.hotel.booking.dto;

import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponse(String id, String propertyId, String roomTypeId, String guestName,
                              LocalDate checkIn, LocalDate checkOut, int guests, BigDecimal totalAmount,
                              BookingStatus status, BigDecimal refundAmount) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.id(), booking.propertyId(), booking.roomTypeId(),
                booking.guestName(), booking.stay().checkIn(), booking.stay().checkOut(), booking.guests(),
                booking.totalAmount(), booking.status(), booking.refundAmount());
    }
}
