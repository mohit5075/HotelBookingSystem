package com.hotel.booking.payment;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class MockPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult charge(String instrument, BigDecimal amount) {
        if (instrument.toLowerCase().contains("decline")) {
            return new PaymentResult(false, null, "Declined");
        }
        return new PaymentResult(true, "TXN-" + UUID.randomUUID(), "Approved");
    }

    @Override
    public PaymentResult refund(String reference, BigDecimal amount) {
        return new PaymentResult(true, "RFD-" + UUID.randomUUID(), "Refunded");
    }
}
