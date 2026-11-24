package com.hotel.booking.search;

import com.hotel.booking.model.Property;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record PropertySearchResult(Property property, List<RoomOffer> offers) {

    public BigDecimal cheapestTotal() {
        return offers.stream().map(RoomOffer::totalPrice).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
    }
}
