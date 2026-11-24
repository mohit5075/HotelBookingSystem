package com.hotel.booking.service;

import com.hotel.booking.cancellation.RefundPolicy;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CancellationService {

    private static final Logger log = LoggerFactory.getLogger(CancellationService.class);

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final RefundPolicy refundPolicy;
    private final StripedLock locks;

    public CancellationService(BookingService bookingService, PaymentService paymentService,
                               RefundPolicy refundPolicy, StripedLock locks) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.refundPolicy = refundPolicy;
        this.locks = locks;
    }

    public Booking cancel(String bookingId) {
        return locks.withLock(bookingId, () -> {
            final Booking booking = bookingService.get(bookingId);
            booking.assertCancellable();

            final BigDecimal refund = booking.status() == BookingStatus.CONFIRMED
                    ? refundPolicy.refundAmount(booking)
                    : BigDecimal.ZERO;
            if (refund.signum() > 0) {
                paymentService.refund(bookingId, refund);
            }
            log.info("Cancelling booking {} with refund {}", bookingId, refund);
            return bookingService.cancel(bookingId, refund);
        });
    }
}
