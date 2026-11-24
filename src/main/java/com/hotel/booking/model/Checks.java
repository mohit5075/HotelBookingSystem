package com.hotel.booking.model;

import com.hotel.booking.exception.InvalidRequestException;
import java.math.BigDecimal;

final class Checks {

    private Checks() {
    }

    static String text(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException(field + " must not be blank");
        }
        return value.trim();
    }

    static int atLeast(int value, int min, String field) {
        if (value < min) {
            throw new InvalidRequestException(field + " must be at least " + min);
        }
        return value;
    }

    static BigDecimal positive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new InvalidRequestException(field + " must be positive");
        }
        return value;
    }
}
