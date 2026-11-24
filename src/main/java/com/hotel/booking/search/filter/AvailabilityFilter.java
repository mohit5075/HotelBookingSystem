package com.hotel.booking.search.filter;

import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.repository.InventoryRepository;
import com.hotel.booking.search.SearchFilter;
import com.hotel.booking.search.SearchQuery;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityFilter implements SearchFilter {

    private final InventoryRepository inventoryRepository;

    public AvailabilityFilter(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public boolean matches(Property property, RoomType roomType, SearchQuery query) {
        return inventoryRepository.availableRooms(roomType.id(), query.stay(), roomType.totalRooms()) > 0;
    }
}
