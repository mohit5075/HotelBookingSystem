package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.booking.cancellation.TimeBasedRefundPolicy;
import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import com.hotel.booking.model.PaymentMethod;
import com.hotel.booking.model.Property;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CancellationServiceTest {

    private final TestSetup f = new TestSetup();
    private final Property property = f.propertyWithRooms(1);

    private Booking paidBooking(int fromDay) {
        final Booking booking = f.bookingService.book(f.request(property, TestSetup.stay(fromDay, fromDay + 2), 2));
        f.paymentService.pay(booking.id(), "key-" + booking.id(), PaymentMethod.CARD, "tok_abc");
        return booking;
    }

    @Test
    void fullRefundEarly() {
        final Booking booking = paidBooking(5);

        final Booking cancelled = f.cancellationService.cancel(booking.id());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.refundAmount()).isEqualByComparingTo("200");
    }

    @Test
    void halfRefundWithin48h() {
        // today is 2030-01-01T10:00Z, check-in is 2030-01-03T00:00 => 38 hours away
        final Booking booking = paidBooking(2);

        assertThat(f.cancellationService.cancel(booking.id()).refundAmount()).isEqualByComparingTo("100");
    }

    @Test
    void noRefundWithin24h() {
        final Booking booking = paidBooking(1);

        assertThat(f.cancellationService.cancel(booking.id()).refundAmount()).isEqualByComparingTo("0");
    }

    @Test
    void cancelFreesRoom() {
        final Booking booking = paidBooking(5);
        f.cancellationService.cancel(booking.id());

        assertThat(f.bookingService.book(f.request(property, TestSetup.stay(5, 7), 2)).status())
                .isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void unpaidCancelHasNoRefund() {
        final Booking booking = f.bookingService.book(f.request(property, TestSetup.stay(5, 7), 2));

        final Booking cancelled = f.cancellationService.cancel(booking.id());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.refundAmount()).isEqualByComparingTo("0");
    }

    @Test
    void cannotCancelTwice() {
        final Booking booking = paidBooking(5);
        f.cancellationService.cancel(booking.id());

        assertThatThrownBy(() -> f.cancellationService.cancel(booking.id()))
                .isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    void noRefundAfterCheckIn() {
        final Booking started = Booking.create("p", "r", "Rohan", TestSetup.stay(-1, 2), 2, new BigDecimal("300"),
                Instant.now());

        assertThat(new TimeBasedRefundPolicy(f.clock).refundAmount(started)).isEqualByComparingTo("0");
    }
}
