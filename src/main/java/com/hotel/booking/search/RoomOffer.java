package com.hotel.booking.search;

import com.hotel.booking.model.RoomType;
import java.math.BigDecimal;

public record RoomOffer(RoomType roomType, BigDecimal totalPrice, BigDecimal averageNightlyPrice) {
}
