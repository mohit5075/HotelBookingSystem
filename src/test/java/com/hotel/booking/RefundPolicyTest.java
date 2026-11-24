package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.hotel.booking.cancellation.TimeBasedRefundPolicy;
import com.hotel.booking.model.Booking;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

// "now" is 2030-01-01T10:00Z, so a stay starting on day N begins (24 * N - 10) hours from now
class RefundPolicyTest {

    private final TimeBasedRefundPolicy policy = new TimeBasedRefundPolicy(TestSetup.fixedClock());

    private Booking bookingStartingInDays(int days) {
        return Booking.create("p", "r", "Rohan", TestSetup.stay(days, days + 2), 2, new BigDecimal("300"), Instant.now());
    }

    @Test
    void fullRefundWhenMoreThan48HoursAway() {
        assertThat(policy.refundAmount(bookingStartingInDays(5))).isEqualByComparingTo("300");
    }

    @Test
    void halfRefundBetween24And48Hours() {
        assertThat(policy.refundAmount(bookingStartingInDays(2))).isEqualByComparingTo("150");
    }

    @Test
    void noRefundWithin24Hours() {
        assertThat(policy.refundAmount(bookingStartingInDays(1))).isEqualByComparingTo("0");
    }

    @Test
    void noRefundOnceStayHasStarted() {
        assertThat(policy.refundAmount(bookingStartingInDays(-1))).isEqualByComparingTo("0");
    }
}
