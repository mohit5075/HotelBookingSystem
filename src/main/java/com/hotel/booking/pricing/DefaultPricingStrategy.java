package com.hotel.booking.pricing;

import com.hotel.booking.model.DateRange;
import com.hotel.booking.model.RoomType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hotel.pricing.strategy", havingValue = "base", matchIfMissing = true)
public class DefaultPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal totalPrice(RoomType roomType, DateRange stay) {
        return roomType.basePrice().multiply(BigDecimal.valueOf(stay.nights())).setScale(2, RoundingMode.HALF_UP);
    }
}
