package com.hotel.booking.search.filter;

import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.search.SearchFilter;
import com.hotel.booking.search.SearchQuery;
import org.springframework.stereotype.Component;

@Component
public class CityFilter implements SearchFilter {

    @Override
    public boolean matches(Property property, RoomType roomType, SearchQuery query) {
        return query.city() == null || query.city().isBlank()
                || property.location().city().equalsIgnoreCase(query.city().trim());
    }
}
