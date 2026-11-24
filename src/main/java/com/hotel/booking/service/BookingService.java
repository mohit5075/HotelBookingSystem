package com.hotel.booking.service;

import com.hotel.booking.exception.InvalidRequestException;
import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.exception.RoomNotAvailableException;
import com.hotel.booking.model.Booking;
import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.pricing.PricingStrategy;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.InventoryRepository;
import com.hotel.booking.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingStrategy pricingStrategy;

    public BookingService(PropertyRepository propertyRepository, BookingRepository bookingRepository,
                          InventoryRepository inventoryRepository, PricingStrategy pricingStrategy) {
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
        this.inventoryRepository = inventoryRepository;
        this.pricingStrategy = pricingStrategy;
    }

    public Booking book(BookingCommand request) {
        final Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.propertyId()));
        final RoomType roomType = property.findRoomType(request.roomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type", request.roomTypeId()));
        validate(request, roomType);

        final BigDecimal price = pricingStrategy.totalPrice(roomType, request.stay());
        if (!inventoryRepository.tryReserve(roomType.id(), request.stay(), roomType.totalRooms())) {
            throw new RoomNotAvailableException("No " + roomType.name() + " room available for the requested dates");
        }
        return bookingRepository.save(Booking.create(property.id(), roomType.id(), request.guestName(),
                request.stay(), request.guests(), price, Instant.now()));
    }

    public Booking get(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    public Booking confirm(String bookingId) {
        final Booking booking = get(bookingId);
        booking.confirm();
        return bookingRepository.save(booking);
    }

    public Booking failPayment(String bookingId) {
        final Booking booking = get(bookingId);
        booking.markPaymentFailed();
        inventoryRepository.release(booking.roomTypeId(), booking.stay());
        return bookingRepository.save(booking);
    }

    public Booking cancel(String bookingId, BigDecimal refund) {
        final Booking booking = get(bookingId);
        booking.cancel(refund);
        inventoryRepository.release(booking.roomTypeId(), booking.stay());
        return bookingRepository.save(booking);
    }

    private void validate(BookingCommand request, RoomType roomType) {
        if (request.stay().checkIn().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("checkIn must not be in the past");
        }
        if (request.guests() > roomType.capacity()) {
            throw new InvalidRequestException(
                    roomType.name() + " sleeps at most " + roomType.capacity() + " guests");
        }
    }
}
