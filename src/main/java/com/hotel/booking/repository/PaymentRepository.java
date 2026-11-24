package com.hotel.booking.repository;

import com.hotel.booking.model.Payment;
import com.hotel.booking.model.PaymentStatus;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByBookingIdAndStatus(String bookingId, PaymentStatus status);
}
