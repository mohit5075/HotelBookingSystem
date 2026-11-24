package com.hotel.booking.model;

import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {
    PENDING_PAYMENT("Awaiting payment"),
    CONFIRMED("Confirmed"),
    PAYMENT_FAILED("Payment failed"),
    CANCELLED("Cancelled");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean canBeCancelled() {
        return canTransitionTo(CANCELLED);
    }

    public boolean canTransitionTo(BookingStatus next) {
        return nextStatuses().contains(next);
    }

    public Set<BookingStatus> nextStatuses() {
        return switch (this) {
            case PENDING_PAYMENT -> EnumSet.of(CONFIRMED, PAYMENT_FAILED, CANCELLED);
            case CONFIRMED -> EnumSet.of(CANCELLED);
            case PAYMENT_FAILED, CANCELLED -> EnumSet.noneOf(BookingStatus.class);
        };
    }
}
