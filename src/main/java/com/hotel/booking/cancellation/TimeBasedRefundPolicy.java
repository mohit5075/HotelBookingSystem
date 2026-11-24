package com.hotel.booking.cancellation;

import com.hotel.booking.model.Booking;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class TimeBasedRefundPolicy implements RefundPolicy {

    private static final long FULL_REFUND_HOURS = 48;
    private static final long PARTIAL_REFUND_HOURS = 24;
    private static final BigDecimal PARTIAL_REFUND_RATE = new BigDecimal("0.50");

    @Override
    public BigDecimal refundAmount(Booking booking) {
        LocalDateTime checkInTime = booking.stay().checkIn().atStartOfDay();
        long hoursToCheckIn = Duration.between(LocalDateTime.now(), checkInTime).toHours();
        if (hoursToCheckIn >= FULL_REFUND_HOURS) {
            return booking.totalAmount();
        }
        if (hoursToCheckIn >= PARTIAL_REFUND_HOURS) {
            return booking.totalAmount().multiply(PARTIAL_REFUND_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
