package com.hotel.booking.payment;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Stand-in for a real provider: approves everything except instruments containing "decline". */
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(ChargeRequest request) {
        if (request.instrument().toLowerCase().contains("decline")) {
            return new PaymentResult(false, null, "Declined by issuer");
        }
        return new PaymentResult(true, "TXN-" + UUID.randomUUID(), "Approved");
    }

    @Override
    public PaymentResult refund(String gatewayReference, BigDecimal amount) {
        return new PaymentResult(true, "RFD-" + UUID.randomUUID(), "Refunded");
    }
}
