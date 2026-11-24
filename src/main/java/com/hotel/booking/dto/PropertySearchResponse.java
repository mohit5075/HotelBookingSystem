package com.hotel.booking.dto;

import com.hotel.booking.model.Amenity;
import com.hotel.booking.search.PropertySearchResult;
import com.hotel.booking.search.RoomOffer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PropertySearchResponse(String propertyId, String name, String city, String locality,
                                     int starRating, Set<Amenity> amenities, List<OfferResponse> availableRooms) {

    public record OfferResponse(String roomTypeId, String roomType, int capacity, BigDecimal totalPrice,
                                BigDecimal averageNightlyPrice) {

        static OfferResponse from(RoomOffer offer) {
            return new OfferResponse(offer.roomType().id(), offer.roomType().name(), offer.roomType().capacity(),
                    offer.totalPrice(), offer.averageNightlyPrice());
        }
    }

    public static PropertySearchResponse from(PropertySearchResult result) {
        final var property = result.property();
        return new PropertySearchResponse(property.id(), property.name(), property.location().city(),
                property.location().locality(), property.starRating(), property.amenities(),
                result.offers().stream().map(OfferResponse::from).toList());
    }
}
