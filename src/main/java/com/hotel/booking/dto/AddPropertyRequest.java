package com.hotel.booking.dto;

import com.hotel.booking.model.Amenity;
import com.hotel.booking.model.Location;
import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record AddPropertyRequest(
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String locality,
        @Min(1) @Max(5) int starRating,
        Set<Amenity> amenities,
        @NotEmpty @Valid List<RoomTypeRequest> roomTypes) {

    public record RoomTypeRequest(
            @NotBlank String name,
            @Min(1) int capacity,
            @NotNull @DecimalMin("0.01") BigDecimal basePrice,
            @Min(1) int totalRooms) {

        RoomType toDomain() {
            return RoomType.create(name, capacity, basePrice, totalRooms);
        }
    }

    public Property toDomain(String ownerId) {
        return Property.create(ownerId, name, new Location(city, locality), starRating, amenities,
                roomTypes.stream().map(RoomTypeRequest::toDomain).toList());
    }
}
