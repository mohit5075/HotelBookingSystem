package com.hotel.booking.search.filter;

import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.search.SearchFilter;
import com.hotel.booking.search.SearchQuery;
import org.springframework.stereotype.Component;

@Component
public class StarRatingFilter implements SearchFilter {

    @Override
    public boolean matches(Property property, RoomType roomType, SearchQuery query) {
        return query.minStarRating() == null || property.starRating() >= query.minStarRating();
    }
}
