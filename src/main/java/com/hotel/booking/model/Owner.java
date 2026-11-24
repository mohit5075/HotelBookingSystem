package com.hotel.booking.model;

import java.util.UUID;

public record Owner(String id, String name) {

    public static Owner create(String name) {
        return new Owner(UUID.randomUUID().toString(), name);
    }
}
