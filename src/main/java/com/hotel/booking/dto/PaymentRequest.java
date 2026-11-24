package com.hotel.booking.dto;

import com.hotel.booking.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** {@code instrument}: card token (tok_...), UPI id (name@bank) or wallet id. */
public record PaymentRequest(@NotNull PaymentMethod method, @NotBlank String instrument) {
}
