package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.model.DateRange;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangeTest {

    @Test
    void nightsExcludeCheckoutDay() {
        final DateRange range = new DateRange(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 4));

        assertThat(range.nights()).isEqualTo(3);
        assertThat(range.dates()).containsExactly(
                LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2), LocalDate.of(2030, 1, 3));
    }

    @Test
    void checkoutMustBeAfterCheckin() {
        final LocalDate day = LocalDate.of(2030, 1, 1);

        assertThatThrownBy(() -> new DateRange(day, day)).isInstanceOf(InvalidRequestException.class);
    }
}
