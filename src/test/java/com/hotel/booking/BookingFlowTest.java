package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.exception.RoomNotAvailableException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import com.hotel.booking.model.PaymentMethod;
import com.hotel.booking.model.Property;
import org.junit.jupiter.api.Test;

/** Payment and cancellation wired to real in-memory inventory, to check rooms are held and released. */
class BookingFlowTest {

    private final TestSetup app = new TestSetup();
    private final Property property = app.propertyWithRooms(1);

    private Booking book(int from, int to) {
        return app.bookingService.book(app.request(property, TestSetup.stay(from, to), 2));
    }

    @Test
    void failedPaymentReleasesTheRoom() {
        Booking booking = book(5, 7);
        app.paymentService.pay(booking.id(), "k1", PaymentMethod.CARD, "tok_decline");

        assertThat(app.bookingService.get(booking.id()).status()).isEqualTo(BookingStatus.PAYMENT_FAILED);
        assertThat(book(5, 7).status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void cancellingAPaidBookingRefundsAndReleasesTheRoom() {
        Booking booking = book(5, 7);
        app.paymentService.pay(booking.id(), "k1", PaymentMethod.UPI, "rohan@okbank");

        Booking cancelled = app.cancellationService.cancel(booking.id());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.refundAmount()).isEqualByComparingTo("200");
        assertThat(book(5, 7)).isNotNull();
    }

    @Test
    void cancelledBookingCannotBePaid() {
        Booking booking = book(5, 7);
        app.cancellationService.cancel(booking.id());

        assertThatThrownBy(() -> app.paymentService.pay(booking.id(), "k1", PaymentMethod.CARD, "tok_1"))
                .isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    void secondCancelDoesNotReleaseTheRoomAgain() {
        Property twoRooms = app.propertyWithRooms(2);
        Booking first = app.bookingService.book(app.request(twoRooms, TestSetup.stay(5, 7), 2));
        app.bookingService.book(app.request(twoRooms, TestSetup.stay(5, 7), 2));
        app.cancellationService.cancel(first.id());

        assertThatThrownBy(() -> app.cancellationService.cancel(first.id()))
                .isInstanceOf(InvalidBookingStateException.class);

        // one room was freed, not two: the next booking works and the one after is sold out
        app.bookingService.book(app.request(twoRooms, TestSetup.stay(5, 7), 2));
        assertThatThrownBy(() -> app.bookingService.book(app.request(twoRooms, TestSetup.stay(5, 7), 2)))
                .isInstanceOf(RoomNotAvailableException.class);
    }
}
