package com.hotel.booking.payment;

import com.hotel.booking.model.PaymentMethod;

public interface PaymentProcessor {

    PaymentMethod method();

    PaymentResult process(ChargeRequest request);
}
