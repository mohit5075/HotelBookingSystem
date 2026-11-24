package com.hotel.booking.service;

import com.hotel.booking.exception.ConflictException;
import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.exception.PaymentProcessingException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import com.hotel.booking.model.Payment;
import com.hotel.booking.model.PaymentMethod;
import com.hotel.booking.model.PaymentStatus;
import com.hotel.booking.payment.ChargeRequest;
import com.hotel.booking.payment.PaymentGateway;
import com.hotel.booking.payment.PaymentProcessorRegistry;
import com.hotel.booking.payment.PaymentResult;
import com.hotel.booking.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final PaymentProcessorRegistry processors;
    private final PaymentGateway gateway;
    private final StripedLock locks;
    private final Clock clock;

    public PaymentService(PaymentRepository paymentRepository, BookingService bookingService,
                          PaymentProcessorRegistry processors, PaymentGateway gateway,
                          StripedLock locks, Clock clock) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.processors = processors;
        this.gateway = gateway;
        this.locks = locks;
        this.clock = clock;
    }

    /**
     * Charges the booking and lets the outcome drive its state: success confirms, failure
     * fails the booking and releases its inventory. Repeating a call with the same idempotency
     * key returns the original payment without charging again.
     */
    public Payment pay(String bookingId, String idempotencyKey, PaymentMethod method,
                       String instrument) {
        return locks.withLock(bookingId, () -> {
            final var replay = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (replay.isPresent()) {
                return replayOf(replay.get(), bookingId);
            }
            return charge(bookingService.get(bookingId), idempotencyKey, method, instrument);
        });
    }

    /** Refunds a successful payment of the booking. Callers must hold the booking lock. */
    public void refund(String bookingId, BigDecimal amount) {
        final Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS)
                .orElseThrow(() -> new InvalidBookingStateException("No successful payment to refund for " + bookingId));
        final PaymentResult result = gateway.refund(payment.gatewayReference(), amount);
        if (!result.success()) {
            throw new PaymentProcessingException("Refund failed: " + result.message());
        }
        payment.markRefunded(amount);
        paymentRepository.save(payment);
    }

    private Payment charge(Booking booking, String idempotencyKey, PaymentMethod method,
                           String instrument) {
        if (booking.status() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidBookingStateException(
                    "Booking " + booking.id() + " is " + booking.status() + " and cannot be paid");
        }
        final ChargeRequest request = new ChargeRequest(method, instrument, booking.totalAmount(), booking.id());
        final PaymentResult result = processors.forMethod(method).process(request);

        final Payment payment = paymentRepository.save(Payment.create(booking.id(), booking.totalAmount(), method,
                idempotencyKey, result.reference(), result.success(), Instant.now(clock)));
        if (result.success()) {
            bookingService.confirm(booking.id());
        } else {
            bookingService.failPayment(booking.id());
        }
        log.info("Payment {} for booking {} finished with status {}", payment.id(), booking.id(), payment.status());
        return payment;
    }

    private static Payment replayOf(Payment existing, String bookingId) {
        if (!existing.bookingId().equals(bookingId)) {
            throw new ConflictException("Idempotency key was already used for a different booking");
        }
        return existing;
    }
}
