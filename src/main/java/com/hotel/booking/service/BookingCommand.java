package com.hotel.booking.service;

import com.hotel.booking.model.DateRange;

public record BookingCommand(String propertyId, String roomTypeId, String guestName, DateRange stay, int guests) {
}
