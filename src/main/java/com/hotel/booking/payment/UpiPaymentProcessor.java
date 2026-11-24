package com.hotel.booking.payment;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.model.PaymentMethod;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    private static final Pattern UPI_ID = Pattern.compile("^[A-Za-z0-9._-]+@[A-Za-z0-9]+$");

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
        if (!UPI_ID.matcher(request.instrument()).matches()) {
            throw new InvalidRequestException("UPI payments need a UPI id such as name@bank");
        }
        return gateway.charge(request);
    }
}
