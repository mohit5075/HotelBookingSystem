package com.hotel.booking.dto;

import com.hotel.booking.model.Amenity;
import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PropertyResponse(String id, String ownerId, String name, String city, String locality,
                               int starRating, Set<Amenity> amenities, List<RoomTypeResponse> roomTypes) {

    public record RoomTypeResponse(String id, String name, int capacity, BigDecimal basePrice, int totalRooms) {

        static RoomTypeResponse from(RoomType r) {
            return new RoomTypeResponse(r.id(), r.name(), r.capacity(), r.basePrice(), r.totalRooms());
        }
    }

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(property.id(), property.ownerId(), property.name(),
                property.location().city(), property.location().locality(), property.starRating(),
                property.amenities(), property.roomTypes().stream().map(RoomTypeResponse::from).toList());
    }
}
