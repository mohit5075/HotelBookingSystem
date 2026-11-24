package com.hotel.booking.model;

/**
 * Booking lifecycle.
 * <pre>
 * PENDING_PAYMENT -> CONFIRMED -> CANCELLED
 *        |-> PAYMENT_FAILED
 *        |-> CANCELLED
 * </pre>
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED;

    public boolean canTransitionTo(BookingStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> next == CONFIRMED || next == PAYMENT_FAILED || next == CANCELLED;
            case CONFIRMED -> next == CANCELLED;
            case PAYMENT_FAILED, CANCELLED -> false;
        };
    }
}
