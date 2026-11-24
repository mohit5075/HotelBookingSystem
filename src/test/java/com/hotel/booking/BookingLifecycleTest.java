package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BookingLifecycleTest {

    private Booking newBooking() {
        return Booking.create("p", "r", "Rohan", TestSetup.stay(10, 12), 2, new BigDecimal("200"), Instant.now());
    }

    @Test
    void pendingThenConfirmedThenCancelled() {
        final Booking booking = newBooking();
        assertThat(booking.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);

        booking.confirm();
        booking.cancel(new BigDecimal("200"));

        assertThat(booking.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.refundAmount()).isEqualByComparingTo("200");
    }

    @Test
    void confirmAfterCancelFails() {
        final Booking booking = newBooking();
        booking.cancel(BigDecimal.ZERO);

        assertThatThrownBy(booking::confirm).isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    void cannotCancelTwice() {
        final Booking booking = newBooking();
        booking.cancel(BigDecimal.ZERO);

        assertThatThrownBy(booking::assertCancellable).isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    void paymentFailedIsTerminal() {
        final Booking booking = newBooking();
        booking.markPaymentFailed();

        assertThatThrownBy(booking::confirm).isInstanceOf(InvalidBookingStateException.class);
    }
}
