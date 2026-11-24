package com.hotel.booking.service;

import com.hotel.booking.cancellation.RefundPolicy;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class CancellationService {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final RefundPolicy refundPolicy;

    public CancellationService(BookingService bookingService, PaymentService paymentService,
                               RefundPolicy refundPolicy) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.refundPolicy = refundPolicy;
    }

    public Booking cancel(String bookingId) {
        Booking booking = bookingService.get(bookingId);
        synchronized (booking) {
            booking.assertCancellable();

            BigDecimal refund = BigDecimal.ZERO;
            if (booking.status() == BookingStatus.CONFIRMED) {
                refund = refundPolicy.refundAmount(booking);
                if (refund.signum() > 0) {
                    paymentService.refund(bookingId, refund);
                }
            }
            return bookingService.cancel(bookingId, refund);
        }
    }
}
