package com.hotel.booking.payment;

public record PaymentResult(boolean success, String reference, String message) {
}
