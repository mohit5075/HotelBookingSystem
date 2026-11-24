package com.hotel.booking.search.filter;

import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.pricing.PricingStrategy;
import com.hotel.booking.search.SearchFilter;
import com.hotel.booking.search.SearchQuery;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PriceRangeFilter implements SearchFilter {

    private final PricingStrategy pricingStrategy;

    public PriceRangeFilter(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    @Override
    public boolean matches(Property property, RoomType roomType, SearchQuery query) {
        if (query.minPricePerNight() == null && query.maxPricePerNight() == null) {
            return true;
        }
        final BigDecimal price = pricingStrategy.averageNightlyPrice(roomType, query.stay());
        return (query.minPricePerNight() == null || price.compareTo(query.minPricePerNight()) >= 0)
                && (query.maxPricePerNight() == null || price.compareTo(query.maxPricePerNight()) <= 0);
    }
}
