package com.hotel.booking.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record Property(String id, String ownerId, String name, Location location, int starRating,
                       Set<Amenity> amenities, List<RoomType> roomTypes) {

    public Property {
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        roomTypes = List.copyOf(roomTypes);
    }

    public static Property create(String ownerId, String name, Location location, int starRating,
                                  Set<Amenity> amenities, List<RoomType> roomTypes) {
        return new Property(UUID.randomUUID().toString(), ownerId, name, location, starRating, amenities, roomTypes);
    }

    public Optional<RoomType> findRoomType(String roomTypeId) {
        return roomTypes.stream().filter(r -> r.id().equals(roomTypeId)).findFirst();
    }
}
