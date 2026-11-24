package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.booking.exception.ConflictException;
import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.exception.RoomNotAvailableException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import com.hotel.booking.model.Payment;
import com.hotel.booking.model.PaymentMethod;
import com.hotel.booking.model.PaymentStatus;
import com.hotel.booking.model.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    private final TestSetup f = new TestSetup();
    private Property property;
    private Booking booking;

    @BeforeEach
    void setUp() {
        property = f.propertyWithRooms(1);
        booking = f.bookingService.book(f.request(property, TestSetup.stay(5, 7), 2));
    }

    @Test
    void paymentConfirmsBooking() {
        final Payment payment = f.paymentService.pay(booking.id(), "k1", PaymentMethod.CARD, "tok_abc");

        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.amount()).isEqualByComparingTo("200");
        assertThat(f.bookingService.get(booking.id()).status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void declinedPaymentReleasesRoom() {
        final Payment payment = f.paymentService.pay(booking.id(), "k1", PaymentMethod.WALLET, "decline-wallet");

        assertThat(payment.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(f.bookingService.get(booking.id()).status()).isEqualTo(BookingStatus.PAYMENT_FAILED);
        assertThat(f.bookingService.book(f.request(property, TestSetup.stay(5, 7), 2))).isNotNull();
    }

    @Test
    void retryWithSameKeyDoesNotRecharge() {
        final Payment first = f.paymentService.pay(booking.id(), "k1", PaymentMethod.UPI, "rohan@okbank");
        final Payment retry = f.paymentService.pay(booking.id(), "k1", PaymentMethod.UPI, "rohan@okbank");

        assertThat(retry.id()).isEqualTo(first.id());
        assertThat(f.chargeCount).hasValue(1);
    }

    @Test
    void keyReuseAcrossBookingsConflicts() {
        f.paymentService.pay(booking.id(), "k1", PaymentMethod.CARD, "tok_abc");
        final Booking other = f.bookingService.book(f.request(property, TestSetup.stay(9, 10), 2));

        assertThatThrownBy(() -> f.paymentService.pay(other.id(), "k1", PaymentMethod.CARD, "tok_abc"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void secondPaymentRejected() {
        f.paymentService.pay(booking.id(), "k1", PaymentMethod.CARD, "tok_abc");

        assertThatThrownBy(() -> f.paymentService.pay(booking.id(), "k2", PaymentMethod.CARD, "tok_abc"))
                .isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    void badInstrumentLeavesBookingPending() {
        assertThatThrownBy(() -> f.paymentService.pay(booking.id(), "k1", PaymentMethod.UPI, "not-a-upi-id"))
                .isInstanceOf(InvalidRequestException.class);

        assertThat(f.bookingService.get(booking.id()).status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void failedPaymentFreesRoom() {
        f.paymentService.pay(booking.id(), "k1", PaymentMethod.CARD, "tok_decline");

        final Booking second = f.bookingService.book(f.request(property, TestSetup.stay(5, 7), 2));
        assertThat(second.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThatThrownBy(() -> f.bookingService.book(f.request(property, TestSetup.stay(5, 7), 2)))
                .isInstanceOf(RoomNotAvailableException.class);
    }
}
