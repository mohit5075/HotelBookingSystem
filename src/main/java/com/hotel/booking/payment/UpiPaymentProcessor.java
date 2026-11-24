package com.hotel.booking.payment;

import com.hotel.booking.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    private final PaymentGateway gateway;

    public UpiPaymentProcessor(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }

    @Override
    public PaymentResult process(ChargeRequest request) {
        return gateway.charge(request);
    }
}
