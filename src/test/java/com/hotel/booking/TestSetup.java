package com.hotel.booking;

import com.hotel.booking.cancellation.TimeBasedRefundPolicy;
import com.hotel.booking.model.Amenity;
import com.hotel.booking.model.DateRange;
import com.hotel.booking.model.Location;
import com.hotel.booking.model.Owner;
import com.hotel.booking.model.Property;
import com.hotel.booking.model.RoomType;
import com.hotel.booking.payment.CardPaymentStrategy;
import com.hotel.booking.payment.UpiPaymentStrategy;
import com.hotel.booking.payment.WalletPaymentStrategy;
import com.hotel.booking.pricing.DefaultPricingStrategy;
import com.hotel.booking.pricing.PricingStrategy;
import com.hotel.booking.repository.InventoryRepository;
import com.hotel.booking.repository.PropertyRepository;
import com.hotel.booking.repository.memory.InMemoryBookingRepository;
import com.hotel.booking.repository.memory.InMemoryInventoryRepository;
import com.hotel.booking.repository.memory.InMemoryOwnerRepository;
import com.hotel.booking.repository.memory.InMemoryPaymentRepository;
import com.hotel.booking.repository.memory.InMemoryPropertyRepository;
import com.hotel.booking.search.SearchFilter;
import com.hotel.booking.search.filter.AmenitiesFilter;
import com.hotel.booking.search.filter.AvailabilityFilter;
import com.hotel.booking.search.filter.CapacityFilter;
import com.hotel.booking.search.filter.CityFilter;
import com.hotel.booking.search.filter.LocalityFilter;
import com.hotel.booking.search.filter.PriceRangeFilter;
import com.hotel.booking.search.filter.StarRatingFilter;
import com.hotel.booking.service.BookingService;
import com.hotel.booking.service.CancellationService;
import com.hotel.booking.service.BookingCommand;
import com.hotel.booking.service.OwnerService;
import com.hotel.booking.service.PaymentService;
import com.hotel.booking.service.PropertyService;
import com.hotel.booking.service.SearchService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

public final class TestSetup {

    public static final LocalDate TODAY = LocalDate.of(2030, 1, 1);

    public final Clock clock = fixedClock();
    public final PropertyRepository propertyRepository = new InMemoryPropertyRepository();
    public final InventoryRepository inventoryRepository = new InMemoryInventoryRepository();
    public final PricingStrategy pricing = new DefaultPricingStrategy();

    public final OwnerService ownerService = new OwnerService(new InMemoryOwnerRepository());
    public final PropertyService propertyService = new PropertyService(propertyRepository, ownerService);
    public final BookingService bookingService = new BookingService(propertyRepository,
            new InMemoryBookingRepository(), inventoryRepository, pricing, clock);
    public final PaymentService paymentService;
    public final CancellationService cancellationService;
    public final SearchService searchService;

    public TestSetup() {
        paymentService = new PaymentService(new InMemoryPaymentRepository(), bookingService,
                List.of(new CardPaymentStrategy(), new UpiPaymentStrategy(), new WalletPaymentStrategy()), clock);
        cancellationService = new CancellationService(bookingService, paymentService,
                new TimeBasedRefundPolicy(clock));
        final List<SearchFilter> filters = List.of(new CityFilter(), new LocalityFilter(), new StarRatingFilter(),
                new AmenitiesFilter(), new CapacityFilter(), new PriceRangeFilter(pricing),
                new AvailabilityFilter(inventoryRepository));
        searchService = new SearchService(propertyRepository, filters, pricing);
    }

    public static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2030-01-01T10:00:00Z"), ZoneOffset.UTC);
    }

    public static DateRange stay(int fromDay, int toDay) {
        return new DateRange(TODAY.plusDays(fromDay), TODAY.plusDays(toDay));
    }

    public Property propertyWithRooms(int totalRooms) {
        return property("Sea Breeze", "Goa", "Calangute", 4, Set.of(Amenity.WIFI), new BigDecimal("100"), totalRooms);
    }

    public Property property(String name, String city, String locality, int stars,
                             Set<Amenity> amenities, BigDecimal price, int totalRooms) {
        final Owner owner = ownerService.create("Owner: " + name);
        final RoomType room = RoomType.create("Standard", 2, price, totalRooms);
        return propertyService.register(
                Property.create(owner.id(), name, new Location(city, locality), stars, amenities, List.of(room)));
    }

    public BookingCommand request(Property property, DateRange stay, int guests) {
        return new BookingCommand(property.id(), property.roomTypes().get(0).id(), "Rohan", stay, guests);
    }
}
