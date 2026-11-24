package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.hotel.booking.model.DateRange;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.pricing.WeekendSurchargePricingStrategy;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PricingStrategyTest {

    @Test
    void weekendSurcharge() {
        final RoomType room = RoomType.create("Standard", 2, new BigDecimal("100"), 1);
        // 2030-01-03 is a Thursday: Thu (100) + Fri (120) + Sat (120) + Sun (100)
        final DateRange stay = new DateRange(LocalDate.of(2030, 1, 3), LocalDate.of(2030, 1, 7));

        assertThat(new WeekendSurchargePricingStrategy().totalPrice(room, stay)).isEqualByComparingTo("440");
    }
}
