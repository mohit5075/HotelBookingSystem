package com.hotel.booking.pricing;

import com.hotel.booking.model.DateRange;
import com.hotel.booking.model.RoomType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hotel.pricing.strategy", havingValue = "weekend")
public class WeekendSurchargePricingStrategy implements PricingStrategy {

    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.20");

    @Override
    public BigDecimal totalPrice(RoomType roomType, DateRange stay) {
        return stay.dates()
                .map(night -> nightlyPrice(roomType, night))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nightlyPrice(RoomType roomType, LocalDate night) {
        final boolean weekend = night.getDayOfWeek() == DayOfWeek.FRIDAY
                || night.getDayOfWeek() == DayOfWeek.SATURDAY;
        return weekend ? roomType.basePrice().multiply(WEEKEND_MULTIPLIER) : roomType.basePrice();
    }
}
