package com.hotel.booking.service;

import com.hotel.booking.exception.ConflictException;
import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.exception.PaymentProcessingException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import com.hotel.booking.model.Payment;
import com.hotel.booking.model.PaymentMethod;
import com.hotel.booking.model.PaymentStatus;
import com.hotel.booking.payment.PaymentStrategy;
import com.hotel.booking.payment.PaymentResult;
import com.hotel.booking.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final Map<PaymentMethod, PaymentStrategy> strategies = new EnumMap<>(PaymentMethod.class);

    public PaymentService(PaymentRepository paymentRepository, BookingService bookingService,
                          List<PaymentStrategy> availableStrategies) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        availableStrategies.forEach(strategy -> strategies.put(strategy.method(), strategy));
    }

    public Payment pay(String bookingId, String idempotencyKey, PaymentMethod method, String instrument) {
        Booking booking = bookingService.get(bookingId);

        // Serialises pay/cancel on one booking inside this JVM only; see README.
        synchronized (booking) {
            Optional<Payment> previous = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (previous.isPresent()) {
                return replay(previous.get(), bookingId);
            }
            if (booking.status() != BookingStatus.PENDING_PAYMENT) {
                throw new InvalidBookingStateException(
                        "Booking " + bookingId + " is " + booking.status().label() + " and cannot be paid");
            }

            PaymentResult result = strategyFor(method).charge(instrument, booking.totalAmount());
            Payment payment = paymentRepository.save(Payment.create(bookingId, booking.totalAmount(), method,
                    idempotencyKey, result.reference(), result.success(), Instant.now()));

            if (result.success()) {
                bookingService.confirm(bookingId);
            } else {
                bookingService.failPayment(bookingId);
            }
            log.info("Payment {} for booking {} is {}", payment.id(), bookingId, payment.status());
            return payment;
        }
    }

    public void refund(String bookingId, BigDecimal amount) {
        Payment payment = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.SUCCESS)
                .orElseThrow(() -> new InvalidBookingStateException("No successful payment to refund for " + bookingId));
        if (amount.compareTo(payment.amount()) > 0) {
            throw new InvalidRequestException("Refund " + amount + " exceeds the amount paid " + payment.amount());
        }

        PaymentResult result = strategyFor(payment.method()).refund(payment.gatewayReference(), amount);
        if (!result.success()) {
            throw new PaymentProcessingException("Refund failed: " + result.message());
        }
        payment.markRefunded(amount);
        paymentRepository.save(payment);
    }

    private PaymentStrategy strategyFor(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new InvalidRequestException("Unsupported payment method: " + method);
        }
        return strategy;
    }

    private Payment replay(Payment previous, String bookingId) {
        if (!previous.bookingId().equals(bookingId)) {
            throw new ConflictException("Idempotency key was already used for a different booking");
        }
        return previous;
    }
}
