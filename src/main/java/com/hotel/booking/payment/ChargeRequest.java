package com.hotel.booking.payment;

import com.hotel.booking.model.PaymentMethod;
import java.math.BigDecimal;

public record ChargeRequest(PaymentMethod method, String instrument, BigDecimal amount, String reference) {
}
