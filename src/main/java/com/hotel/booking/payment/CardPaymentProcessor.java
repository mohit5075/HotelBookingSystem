package com.hotel.booking.payment;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.model.PaymentMethod;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor implements PaymentProcessor {

    private static final Pattern CARD_TOKEN = Pattern.compile("^tok_[A-Za-z0-9_]+$");

    private final PaymentGateway gateway;

    public CardPaymentProcessor(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentResult process(ChargeRequest request) {
        if (!CARD_TOKEN.matcher(request.instrument()).matches()) {
            throw new InvalidRequestException("Card payments need a card token such as tok_abc123");
        }
        return gateway.charge(request);
    }
}
