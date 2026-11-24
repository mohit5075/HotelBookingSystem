package com.hotel.booking.repository.memory;

import com.hotel.booking.model.Payment;
import com.hotel.booking.model.PaymentStatus;
import com.hotel.booking.repository.PaymentRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    @Override
    public Payment save(Payment payment) {
        payments.put(payment.id(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return payments.values().stream()
                .filter(p -> p.idempotencyKey().equals(idempotencyKey))
                .findFirst();
    }

    @Override
    public Optional<Payment> findByBookingIdAndStatus(String bookingId, PaymentStatus status) {
        return payments.values().stream()
                .filter(p -> p.bookingId().equals(bookingId) && p.status() == status)
                .findFirst();
    }
}
