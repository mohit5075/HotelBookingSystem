package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.exception.RoomNotAvailableException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.BookingStatus;
import com.hotel.booking.model.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private final TestSetup f = new TestSetup();

    @Test
    void newBookingIsPendingWithPrice() {
        final Property property = f.propertyWithRooms(1);

        final Booking booking = f.bookingService.book(f.request(property, TestSetup.stay(5, 8), 2));

        assertThat(booking.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(booking.totalAmount()).isEqualByComparingTo("300");
    }

    @Test
    void overlapRejectedWhenSoldOut() {
        final Property property = f.propertyWithRooms(1);
        f.bookingService.book(f.request(property, TestSetup.stay(5, 8), 2));

        assertThatThrownBy(() -> f.bookingService.book(f.request(property, TestSetup.stay(7, 10), 2)))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void backToBackStaysAllowed() {
        final Property property = f.propertyWithRooms(1);
        f.bookingService.book(f.request(property, TestSetup.stay(5, 8), 2));

        final Booking next = f.bookingService.book(f.request(property, TestSetup.stay(8, 10), 2));

        assertThat(next.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void tooManyGuestsAndPastDates() {
        final Property property = f.propertyWithRooms(1);

        assertThatThrownBy(() -> f.bookingService.book(f.request(property, TestSetup.stay(5, 8), 3)))
                .isInstanceOf(InvalidRequestException.class).hasMessageContaining("at most 2");
        assertThatThrownBy(() -> f.bookingService.book(f.request(property, TestSetup.stay(-3, 2), 2)))
                .isInstanceOf(InvalidRequestException.class).hasMessageContaining("past");
    }

    @Test
    void noOverbookingUnderConcurrency() throws Exception {
        final int rooms = 3;
        final int attempts = 20;
        final Property property = f.propertyWithRooms(rooms);
        final ExecutorService pool = Executors.newFixedThreadPool(attempts);
        final CountDownLatch start = new CountDownLatch(1);
        final AtomicInteger succeeded = new AtomicInteger();

        final List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    f.bookingService.book(f.request(property, TestSetup.stay(5, 8), 2));
                    succeeded.incrementAndGet();
                } catch (RoomNotAvailableException expected) {
                    // lost the race for the last rooms
                }
                return null;
            }));
        }
        start.countDown();
        for (final Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        assertThat(succeeded).hasValue(rooms);
    }
}
