package com.hotel.booking.search;

import com.hotel.booking.model.Amenity;
import com.hotel.booking.model.DateRange;
import java.math.BigDecimal;
import java.util.Set;

public record SearchQuery(String city, String locality, DateRange stay, int guests,
                          BigDecimal minPricePerNight, BigDecimal maxPricePerNight,
                          Set<Amenity> amenities, Integer minStarRating) {

    public SearchQuery {
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
    }
}
