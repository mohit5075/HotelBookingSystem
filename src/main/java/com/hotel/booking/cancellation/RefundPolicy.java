package com.hotel.booking.cancellation;

import com.hotel.booking.model.Booking;
import java.math.BigDecimal;

public interface RefundPolicy {

    BigDecimal refundAmount(Booking booking);
}
