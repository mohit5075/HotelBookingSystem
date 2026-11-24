package com.hotel.booking.dto;

import com.hotel.booking.model.Owner;

public record OwnerResponse(String id, String name) {

    public static OwnerResponse from(Owner owner) {
        return new OwnerResponse(owner.id(), owner.name());
    }
}
