package com.hotel.booking.search;

import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;

/**
 * One search criterion. Every Spring bean implementing this interface is applied automatically,
 * so a new filter is just a new class; {@link com.hotel.booking.service.SearchService} never changes.
 * A filter whose criterion is absent from the query must return true.
 */
public interface SearchFilter {

    boolean matches(Property property, RoomType roomType, SearchQuery query);
}
