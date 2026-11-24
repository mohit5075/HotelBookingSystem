package com.hotel.booking.search;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.model.Amenity;
import com.hotel.booking.model.DateRange;
import java.math.BigDecimal;
import java.util.Set;

public record SearchQuery(String city, String locality, DateRange stay, int guests,
                          BigDecimal minPricePerNight, BigDecimal maxPricePerNight,
                          Set<Amenity> amenities, Integer minStarRating) {

    public SearchQuery {
        if (stay == null) {
            throw new InvalidRequestException("check-in and check-out dates are required");
        }
        if (guests < 1) {
            throw new InvalidRequestException("guests must be at least 1");
        }
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
    }
}
