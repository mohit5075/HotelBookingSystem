package com.hotel.booking.model;

import java.math.BigDecimal;
import java.util.UUID;

public record RoomType(String id, String name, int capacity, BigDecimal basePrice, int totalRooms) {

    public static RoomType create(String name, int capacity, BigDecimal basePrice, int totalRooms) {
        return new RoomType(UUID.randomUUID().toString(), name, capacity, basePrice, totalRooms);
    }
}
