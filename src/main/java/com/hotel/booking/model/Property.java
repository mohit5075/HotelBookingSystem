package com.hotel.booking.model;

import com.hotel.booking.exception.InvalidRequestException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record Property(String id, String ownerId, String name, Location location, int starRating,
                       Set<Amenity> amenities, List<RoomType> roomTypes) {

    private static final int MIN_STARS = 1;
    private static final int MAX_STARS = 5;

    public Property {
        id = Checks.text(id, "property id");
        ownerId = Checks.text(ownerId, "ownerId");
        name = Checks.text(name, "property name");
        if (location == null) {
            throw new InvalidRequestException("location is required");
        }
        if (starRating < MIN_STARS || starRating > MAX_STARS) {
            throw new InvalidRequestException("starRating must be between 1 and 5");
        }
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        if (roomTypes == null || roomTypes.isEmpty()) {
            throw new InvalidRequestException("a property needs at least one room type");
        }
        roomTypes = List.copyOf(roomTypes);
    }

    public static Property create(String ownerId, String name, Location location,
                                  int starRating, Set<Amenity> amenities,
                                  List<RoomType> roomTypes) {
        return new Property(UUID.randomUUID().toString(), ownerId, name, location, starRating, amenities, roomTypes);
    }

    public Optional<RoomType> findRoomType(String roomTypeId) {
        return roomTypes.stream().filter(r -> r.id().equals(roomTypeId)).findFirst();
    }
}
