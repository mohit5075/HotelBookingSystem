package com.hotel.booking.dto;

import com.hotel.booking.model.Amenity;
import com.hotel.booking.model.DateRange;
import com.hotel.booking.search.SearchQuery;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;

/** Query-string parameters of the property search. Dates are ISO (yyyy-MM-dd). */
public record SearchRequest(
        String city,
        String locality,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
        @Min(1) int guests,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Set<Amenity> amenities,
        @Min(1) Integer minStarRating) {

    public SearchQuery toQuery() {
        return new SearchQuery(city, locality, new DateRange(checkIn, checkOut), guests, minPrice, maxPrice,
                amenities, minStarRating);
    }
}
