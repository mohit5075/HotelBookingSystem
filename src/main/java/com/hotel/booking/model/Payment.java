package com.hotel.booking.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Payment {

    private final String id;
    private final String bookingId;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final String idempotencyKey;
    private final String gatewayReference;
    private final Instant createdAt;
    private volatile PaymentStatus status;
    private volatile BigDecimal refundedAmount = BigDecimal.ZERO;

    private Payment(String bookingId, BigDecimal amount, PaymentMethod method,
                    String idempotencyKey, String gatewayReference, PaymentStatus status,
                    Instant createdAt) {
        this.id = UUID.randomUUID().toString();
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.gatewayReference = gatewayReference;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Payment create(String bookingId, BigDecimal amount, PaymentMethod method,
                                 String idempotencyKey, String gatewayReference,
                                 boolean successful, Instant createdAt) {
        final PaymentStatus status = successful ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        return new Payment(bookingId, amount, method, idempotencyKey, gatewayReference, status, createdAt);
    }

    public synchronized void markRefunded(BigDecimal refund) {
        this.refundedAmount = refund;
        this.status = PaymentStatus.REFUNDED;
    }

    public String id() {
        return id;
    }

    public String bookingId() {
        return bookingId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public PaymentMethod method() {
        return method;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public String gatewayReference() {
        return gatewayReference;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public PaymentStatus status() {
        return status;
    }

    public BigDecimal refundedAmount() {
        return refundedAmount;
    }
}
