package com.hotel.booking.model;

import java.math.BigDecimal;
import java.util.UUID;

public record RoomType(String id, String name, int capacity, BigDecimal basePrice, int totalRooms) {

    public RoomType {
        id = Checks.text(id, "roomType id");
        name = Checks.text(name, "roomType name");
        Checks.atLeast(capacity, 1, "capacity");
        Checks.positive(basePrice, "basePrice");
        Checks.atLeast(totalRooms, 1, "totalRooms");
    }

    public static RoomType create(String name, int capacity, BigDecimal basePrice,
                                  int totalRooms) {
        return new RoomType(UUID.randomUUID().toString(), name, capacity, basePrice, totalRooms);
    }
}
