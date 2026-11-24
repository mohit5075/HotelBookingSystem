package com.hotel.booking.pricing;

import com.hotel.booking.model.DateRange;
import com.hotel.booking.model.RoomType;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Decides what a stay costs. Swap implementations for dynamic pricing. */
public interface PricingStrategy {

    BigDecimal totalPrice(RoomType roomType, DateRange stay);

    default BigDecimal averageNightlyPrice(RoomType roomType, DateRange stay) {
        return totalPrice(roomType, stay).divide(BigDecimal.valueOf(stay.nights()), 2, RoundingMode.HALF_UP);
    }
}
