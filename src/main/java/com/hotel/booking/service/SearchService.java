package com.hotel.booking.service;

import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.pricing.PricingStrategy;
import com.hotel.booking.repository.PropertyRepository;
import com.hotel.booking.search.PropertySearchResult;
import com.hotel.booking.search.RoomOffer;
import com.hotel.booking.search.SearchFilter;
import com.hotel.booking.search.SearchQuery;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final PropertyRepository propertyRepository;
    private final List<SearchFilter> filters;
    private final PricingStrategy pricingStrategy;

    public SearchService(PropertyRepository propertyRepository, List<SearchFilter> filters,
                         PricingStrategy pricingStrategy) {
        this.propertyRepository = propertyRepository;
        this.filters = List.copyOf(filters);
        this.pricingStrategy = pricingStrategy;
    }

    public List<PropertySearchResult> search(SearchQuery query) {
        return propertyRepository.findAll().stream()
                .map(property -> toResult(property, query))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(PropertySearchResult::cheapestTotal))
                .toList();
    }

    private Optional<PropertySearchResult> toResult(Property property, SearchQuery query) {
        final List<RoomOffer> offers = property.roomTypes().stream()
                .filter(roomType -> matchesAll(property, roomType, query))
                .map(roomType -> toOffer(roomType, query))
                .toList();
        return offers.isEmpty() ? Optional.empty() : Optional.of(new PropertySearchResult(property, offers));
    }

    private boolean matchesAll(Property property, RoomType roomType, SearchQuery query) {
        return filters.stream().allMatch(filter -> filter.matches(property, roomType, query));
    }

    private RoomOffer toOffer(RoomType roomType, SearchQuery query) {
        return new RoomOffer(roomType,
                pricingStrategy.totalPrice(roomType, query.stay()),
                pricingStrategy.averageNightlyPrice(roomType, query.stay()));
    }
}
