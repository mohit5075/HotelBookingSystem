package com.hotel.booking.payment;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentResult charge(ChargeRequest request);

    PaymentResult refund(String gatewayReference, BigDecimal amount);
}
