package com.hotel.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOwnerRequest(@NotBlank String name) {
}
