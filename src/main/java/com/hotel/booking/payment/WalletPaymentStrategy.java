package com.hotel.booking.payment;

import com.hotel.booking.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class WalletPaymentStrategy extends MockPaymentStrategy {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.WALLET;
    }
}
