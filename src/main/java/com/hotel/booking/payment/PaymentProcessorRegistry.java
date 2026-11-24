package com.hotel.booking.payment;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.model.PaymentMethod;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorRegistry {

    private final Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);

    public PaymentProcessorRegistry(List<PaymentProcessor> available) {
        available.forEach(p -> processors.put(p.method(), p));
    }

    public PaymentProcessor forMethod(PaymentMethod method) {
        final PaymentProcessor processor = processors.get(method);
        if (processor == null) {
            throw new InvalidRequestException("Unsupported payment method: " + method);
        }
        return processor;
    }
}
