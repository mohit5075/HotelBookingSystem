package com.hotel.booking.payment;

import com.hotel.booking.model.PaymentMethod;
import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentMethod method();
    PaymentResult charge(String instrument, BigDecimal amount);
    PaymentResult refund(String reference, BigDecimal amount);
}
