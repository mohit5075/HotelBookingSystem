package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.dto.CreateBookingRequest;
import com.hotel.booking.dto.PaymentRequest;
import com.hotel.booking.dto.PaymentResponse;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.CancellationService;
import com.hotel.booking.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final CancellationService cancellationService;

    public BookingController(BookingService bookingService, PaymentService paymentService,
                             CancellationService cancellationService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.cancellationService = cancellationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody CreateBookingRequest request) {
        return BookingResponse.from(bookingService.book(request.toNewBooking()));
    }

    @GetMapping("/{bookingId}")
    public BookingResponse get(@PathVariable String bookingId) {
        return BookingResponse.from(bookingService.get(bookingId));
    }

    @PostMapping("/{bookingId}/payments")
    public PaymentResponse pay(@PathVariable String bookingId,
                               @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
                               @Valid @RequestBody PaymentRequest request) {
        return PaymentResponse.from(
                paymentService.pay(bookingId, idempotencyKey, request.method(), request.instrument()));
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancel(@PathVariable String bookingId) {
        return BookingResponse.from(cancellationService.cancel(bookingId));
    }
}
