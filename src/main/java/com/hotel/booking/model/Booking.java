package com.hotel.booking.model;

import com.hotel.booking.exception.InvalidBookingStateException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Booking {

    private final String id;
    private final String propertyId;
    private final String roomTypeId;
    private final String guestName;
    private final DateRange stay;
    private final int guests;
    private final BigDecimal totalAmount;
    private final Instant createdAt;
    private volatile BookingStatus status = BookingStatus.PENDING_PAYMENT;
    private volatile BigDecimal refundAmount = BigDecimal.ZERO;

    private Booking(String propertyId, String roomTypeId, String guestName,
                    DateRange stay, int guests, BigDecimal totalAmount, Instant createdAt) {
        this.id = UUID.randomUUID().toString();
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.guestName = guestName;
        this.stay = stay;
        this.guests = guests;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public static Booking create(String propertyId, String roomTypeId, String guestName,
                                 DateRange stay, int guests, BigDecimal totalAmount,
                                 Instant createdAt) {
        return new Booking(propertyId, roomTypeId, guestName, stay, guests, totalAmount, createdAt);
    }

    public synchronized void confirm() {
        transitionTo(BookingStatus.CONFIRMED);
    }

    public synchronized void markPaymentFailed() {
        transitionTo(BookingStatus.PAYMENT_FAILED);
    }

    public synchronized void cancel(BigDecimal refund) {
        transitionTo(BookingStatus.CANCELLED);
        this.refundAmount = refund;
    }

    public void assertCancellable() {
        if (!status.canBeCancelled()) {
            throw invalidTransition(BookingStatus.CANCELLED);
        }
    }

    private void transitionTo(BookingStatus next) {
        if (!status.canTransitionTo(next)) {
            throw invalidTransition(next);
        }
        this.status = next;
    }

    private InvalidBookingStateException invalidTransition(BookingStatus next) {
        return new InvalidBookingStateException(
                "Booking " + id + " is " + status.label() + " and cannot be changed to " + next.label());
    }

    public String id() {
        return id;
    }

    public String propertyId() {
        return propertyId;
    }

    public String roomTypeId() {
        return roomTypeId;
    }

    public String guestName() {
        return guestName;
    }

    public DateRange stay() {
        return stay;
    }

    public int guests() {
        return guests;
    }

    public BigDecimal totalAmount() {
        return totalAmount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public BookingStatus status() {
        return status;
    }

    public BigDecimal refundAmount() {
        return refundAmount;
    }
}
