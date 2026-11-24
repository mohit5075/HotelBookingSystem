package com.hotel.booking.repository;

import com.hotel.booking.model.Booking;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(String id);
}
