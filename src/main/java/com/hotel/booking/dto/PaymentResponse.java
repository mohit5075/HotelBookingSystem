package com.hotel.booking.dto;

import com.hotel.booking.model.Payment;
import com.hotel.booking.model.PaymentMethod;
import com.hotel.booking.model.PaymentStatus;
import java.math.BigDecimal;

public record PaymentResponse(String id, String bookingId, BigDecimal amount, PaymentMethod method,
                              PaymentStatus status, BigDecimal refundedAmount) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.id(), payment.bookingId(), payment.amount(), payment.method(),
                payment.status(), payment.refundedAmount());
    }
}
