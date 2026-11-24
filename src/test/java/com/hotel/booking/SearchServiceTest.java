package com.hotel.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.hotel.booking.model.Amenity;
import com.hotel.booking.model.Property;
import com.hotel.booking.search.PropertySearchResult;
import com.hotel.booking.search.SearchQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchServiceTest {

    private final TestSetup f = new TestSetup();
    private Property goaBudget;
    private Property goaLuxury;

    @BeforeEach
    void setUp() {
        goaBudget = f.property("City Hostel", "Goa", "Calangute", 2, Set.of(Amenity.WIFI), new BigDecimal("50"), 1);
        goaLuxury = f.property("Royal Orchid", "Goa", "Baga", 5, Set.of(Amenity.WIFI, Amenity.POOL),
                new BigDecimal("300"), 1);
        f.property("Pine Retreat", "Manali", "Old Manali", 3, Set.of(Amenity.PARKING), new BigDecimal("80"), 1);
    }

    private SearchQuery query(String city, int guests, BigDecimal max, Set<Amenity> amenities,
                              Integer minStars) {
        return new SearchQuery(city, null, TestSetup.stay(5, 7), guests, null, max, amenities, minStars);
    }

    private List<String> names(List<PropertySearchResult> results) {
        return results.stream().map(r -> r.property().name()).toList();
    }

    @Test
    void cityFilterSortedByPrice() {
        assertThat(names(f.searchService.search(query("goa", 2, null, null, null))))
                .containsExactly("City Hostel", "Royal Orchid");
    }

    @Test
    void priceAmenityStarFilters() {
        assertThat(names(f.searchService.search(query("Goa", 2, new BigDecimal("100"), null, null))))
                .containsExactly("City Hostel");
        assertThat(names(f.searchService.search(query("Goa", 2, null, Set.of(Amenity.POOL), null))))
                .containsExactly("Royal Orchid");
        assertThat(names(f.searchService.search(query("Goa", 2, null, null, 4)))).containsExactly("Royal Orchid");
    }

    @Test
    void capacityFilter() {
        assertThat(f.searchService.search(query("Goa", 5, null, null, null))).isEmpty();
    }

    @Test
    void bookedPropertyHidden() {
        f.bookingService.book(f.request(goaBudget, TestSetup.stay(6, 8), 2));

        assertThat(names(f.searchService.search(query("Goa", 2, null, null, null)))).containsExactly("Royal Orchid");
    }

    @Test
    void reappearsAfterCancel() {
        final var booking = f.bookingService.book(f.request(goaLuxury, TestSetup.stay(5, 7), 2));
        f.cancellationService.cancel(booking.id());

        assertThat(names(f.searchService.search(query("Goa", 2, null, null, 5)))).containsExactly("Royal Orchid");
    }
}
