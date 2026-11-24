package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotel.booking.exception.ConflictException;
import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.Payment;
import com.hotel.booking.model.PaymentMethod;
import com.hotel.booking.model.PaymentStatus;
import com.hotel.booking.payment.PaymentStrategy;
import com.hotel.booking.payment.PaymentResult;
import com.hotel.booking.repository.PaymentRepository;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("200");

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingService bookingService;
    @Mock
    private PaymentStrategy cardStrategy;

    private PaymentService paymentService;
    private Booking booking;

    @BeforeEach
    void setUp() {
        lenient().when(cardStrategy.method()).thenReturn(PaymentMethod.CARD);
        paymentService = new PaymentService(paymentRepository, bookingService, List.of(cardStrategy),
                TestSetup.fixedClock());
        booking = Booking.create("p1", "r1", "Rohan", TestSetup.stay(5, 7), 2, AMOUNT, Instant.now());
        lenient().when(bookingService.get(booking.id())).thenReturn(booking);
    }

    private void cardProcessorReturns(PaymentResult result) {
        when(cardStrategy.charge("tok_1", AMOUNT)).thenReturn(result);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void successfulPaymentConfirmsBooking() {
        cardProcessorReturns(new PaymentResult(true, "TXN-1", "Approved"));

        Payment payment = paymentService.pay(booking.id(), "key-1", PaymentMethod.CARD, "tok_1");

        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.amount()).isEqualByComparingTo(AMOUNT);
        verify(bookingService).confirm(booking.id());
        verify(bookingService, never()).failPayment(booking.id());
    }

    @Test
    void failedPaymentFailsBooking() {
        cardProcessorReturns(new PaymentResult(false, null, "Declined"));

        Payment payment = paymentService.pay(booking.id(), "key-1", PaymentMethod.CARD, "tok_1");

        assertThat(payment.status()).isEqualTo(PaymentStatus.FAILED);
        verify(bookingService).failPayment(booking.id());
        verify(bookingService, never()).confirm(booking.id());
    }

    @Test
    void cancelledBookingCannotBePaid() {
        booking.cancel(BigDecimal.ZERO);

        assertThatThrownBy(() -> paymentService.pay(booking.id(), "key-1", PaymentMethod.CARD, "tok_1"))
                .isInstanceOf(InvalidBookingStateException.class);
        verify(cardStrategy, never()).charge(any(), any());
    }

    @Test
    void confirmedBookingCannotBePaidAgain() {
        booking.confirm();

        assertThatThrownBy(() -> paymentService.pay(booking.id(), "key-2", PaymentMethod.CARD, "tok_1"))
                .isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    void sameKeyReturnsFirstPaymentWithoutCharging() {
        Payment first = Payment.create(booking.id(), AMOUNT, PaymentMethod.CARD, "key-1", "TXN-1", true, Instant.now());
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(first));

        Payment retry = paymentService.pay(booking.id(), "key-1", PaymentMethod.CARD, "tok_1");

        assertThat(retry).isSameAs(first);
        verify(cardStrategy, never()).charge(any(), any());
    }

    @Test
    void keyCannotBeReusedForAnotherBooking() {
        Payment other = Payment.create("other-booking", AMOUNT, PaymentMethod.CARD, "key-1", "TXN-1", true, Instant.now());
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> paymentService.pay(booking.id(), "key-1", PaymentMethod.CARD, "tok_1"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void unsupportedMethodIsRejected() {
        assertThatThrownBy(() -> paymentService.pay(booking.id(), "key-1", PaymentMethod.WALLET, "w1"))
                .isInstanceOf(InvalidRequestException.class);
        verify(bookingService, never()).confirm(any());
    }

    @Test
    void refundCannotExceedAmountPaid() {
        Payment paid = Payment.create(booking.id(), AMOUNT, PaymentMethod.CARD, "key-1", "TXN-1", true, Instant.now());
        when(paymentRepository.findByBookingIdAndStatus(booking.id(), PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> paymentService.refund(booking.id(), new BigDecimal("250")))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void refundMarksPaymentRefunded() {
        Payment paid = Payment.create(booking.id(), AMOUNT, PaymentMethod.CARD, "key-1", "TXN-1", true, Instant.now());
        when(paymentRepository.findByBookingIdAndStatus(booking.id(), PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(paid));
        when(cardStrategy.refund("TXN-1", new BigDecimal("100"))).thenReturn(new PaymentResult(true, "RFD-1", "ok"));

        paymentService.refund(booking.id(), new BigDecimal("100"));

        assertThat(paid.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(paid.refundedAmount()).isEqualByComparingTo("100");
    }
}
