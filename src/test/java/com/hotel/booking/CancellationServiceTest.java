package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hotel.booking.cancellation.RefundPolicy;
import com.hotel.booking.exception.InvalidBookingStateException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.CancellationService;
import com.hotel.booking.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancellationServiceTest {

    private static final BigDecimal AMOUNT = new BigDecimal("200");

    @Mock
    private BookingService bookingService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RefundPolicy refundPolicy;

    private CancellationService cancellationService;
    private Booking booking;

    @BeforeEach
    void setUp() {
        cancellationService = new CancellationService(bookingService, paymentService, refundPolicy);
        booking = Booking.create("p1", "r1", "Rohan", TestSetup.stay(5, 7), 2, AMOUNT, Instant.now());
        when(bookingService.get(booking.id())).thenReturn(booking);
    }

    @Test
    void confirmedBookingIsRefundedFullyAndCancelled() {
        booking.confirm();
        when(refundPolicy.refundAmount(booking)).thenReturn(AMOUNT);

        cancellationService.cancel(booking.id());

        verify(paymentService).refund(booking.id(), AMOUNT);
        verify(bookingService).cancel(booking.id(), AMOUNT);
    }

    @Test
    void partialRefundIsPassedThrough() {
        booking.confirm();
        BigDecimal half = new BigDecimal("100");
        when(refundPolicy.refundAmount(booking)).thenReturn(half);

        cancellationService.cancel(booking.id());

        verify(paymentService).refund(booking.id(), half);
        verify(bookingService).cancel(booking.id(), half);
    }

    @Test
    void noRefundWhenPolicyReturnsZero() {
        booking.confirm();
        when(refundPolicy.refundAmount(booking)).thenReturn(BigDecimal.ZERO);

        cancellationService.cancel(booking.id());

        verify(paymentService, never()).refund(any(), any());
        verify(bookingService).cancel(booking.id(), BigDecimal.ZERO);
    }

    @Test
    void unpaidBookingIsCancelledWithoutRefund() {
        cancellationService.cancel(booking.id());

        verifyNoInteractions(refundPolicy, paymentService);
        verify(bookingService).cancel(booking.id(), BigDecimal.ZERO);
    }

    @Test
    void cancellingTwiceFailsWithoutRefundingAgain() {
        booking.cancel(BigDecimal.ZERO);

        assertThatThrownBy(() -> cancellationService.cancel(booking.id()))
                .isInstanceOf(InvalidBookingStateException.class);
        verifyNoInteractions(refundPolicy, paymentService);
        verify(bookingService, never()).cancel(any(), any());
    }

    @Test
    void returnsWhatBookingServiceReturns() {
        when(bookingService.cancel(booking.id(), BigDecimal.ZERO)).thenReturn(booking);

        assertThat(cancellationService.cancel(booking.id())).isSameAs(booking);
    }
}
