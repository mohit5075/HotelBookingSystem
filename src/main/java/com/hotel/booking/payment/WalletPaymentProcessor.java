package com.hotel.booking.payment;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class WalletPaymentProcessor implements PaymentProcessor {

    private final PaymentGateway gateway;

    public WalletPaymentProcessor(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.WALLET;
    }

    @Override
    public PaymentResult process(ChargeRequest request) {
        if (request.instrument().isBlank()) {
            throw new InvalidRequestException("Wallet payments need a wallet id");
        }
        return gateway.charge(request);
    }
}
