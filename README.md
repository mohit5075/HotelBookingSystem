# Hotel Booking Platform (backend)

Spring Boot 3 / Java 17 service for property discovery, onboarding, booking, payment and cancellation.
All data is in memory behind repository interfaces.

## Build and run

```
mvn clean test              # build + 42 unit/integration tests
mvn spring-boot:run         # http://localhost:8080
```

Swagger UI: http://localhost:8080/swagger-ui.html (OpenAPI JSON at `/v3/api-docs`).
Pricing strategy: `hotel.pricing.strategy=base|weekend` in `application.properties`.

## API walk-through

| Step | Request |
|------|---------|
| Create owner (account) | `POST /api/owners` `{"name":"Lakeview Hotels"}` |
| Add property | `POST /api/owners/{ownerId}/properties` (name, city, locality, starRating, amenities, roomTypes[]) |
| List an owner's properties | `GET /api/owners/{ownerId}/properties` |
| Search | `GET /api/properties/search?city=&locality=&checkIn=&checkOut=&guests=&minPrice=&maxPrice=&amenities=WIFI,POOL&minStarRating=` |
| Book | `POST /api/bookings` (propertyId, roomTypeId, guestName, checkIn, checkOut, guests) -> `PENDING_PAYMENT` |
| Pay | `POST /api/bookings/{id}/payments` + header `Idempotency-Key` `{"method":"CARD\|UPI\|WALLET","instrument":"..."}` |
| Cancel | `POST /api/bookings/{id}/cancel` |
| Get booking | `GET /api/bookings/{id}` |

`instrument` is a card token, UPI id or wallet id (any string; the mock gateway declines any value containing `decline`).
Errors use RFC 7807 problem details: 400 validation, 404 not found, 409 conflict (sold out, invalid state, idempotency).

## Architecture

`controller` (REST, validation, DTO mapping) -> `service` (business rules) -> `repository` interfaces (in-memory implementations in `repository.memory`). The domain model (`model`) holds the entities and the booking state machine. Extension points are interfaces: `SearchFilter`, `PricingStrategy`, `RefundPolicy`, `PaymentStrategy` and the repositories.

- **Owner -> Property -> RoomType.** A standalone host is simply an owner with one property; there is no separate single-property path.
- **Search** applies every `SearchFilter` bean (city, locality, stars, amenities, capacity, price, availability). A new filter is a new class; `SearchService` does not change.
- **Pricing:** `PricingStrategy` (base price x nights by default; `hotel.pricing.strategy=weekend` adds a Friday/Saturday surcharge).

## Booking lifecycle

```
PENDING_PAYMENT -> CONFIRMED -> CANCELLED
       |-> PAYMENT_FAILED
       |-> CANCELLED
```
`Booking` rejects illegal transitions with `InvalidBookingStateException` (409). A booking holds its room from creation until it is paid, fails or is cancelled.

## Payment flow

`PaymentService.pay`: fetch booking -> must be `PENDING_PAYMENT` -> charge through the `PaymentStrategy` for the chosen method -> save the payment -> confirm the booking on success, or mark it `PAYMENT_FAILED` and release the rooms on failure. Card, UPI and wallet strategies share a mock implementation (`MockPaymentStrategy`) that declines any instrument containing "decline". The same `Idempotency-Key` returns the first payment without charging again; reusing a key for a different booking is a 409.

## Cancellation and refund flow

`CancellationService.cancel`: fetch booking -> must be cancellable -> `RefundPolicy` calculates the refund (only for confirmed bookings) -> refund through the payment strategy (never more than was paid) -> cancel the booking and release the rooms. The default `TimeBasedRefundPolicy` refunds 100% at least 48h before check-in, 50% at least 24h before, otherwise nothing. A second cancel fails before anything is refunded or released.

## Inventory and concurrency

Rooms held per room type per night are tracked in `InventoryRepository`. `tryReserve` checks every night of the stay and holds them in one step, and the check-out day is not held. In the in-memory implementation every method is `synchronized`, which prevents overbooking (a test fires 20 concurrent requests at 3 rooms and exactly 3 succeed). Payment and cancellation also synchronize on the `Booking` object so one booking is not paid and cancelled at the same time.

**Limitation:** this is JVM-level locking only. It does not protect against several application instances or against a repository that returns copies instead of shared objects. A real database would need a conditional update or row lock for inventory.

## Assumptions

- One booking = one room of the chosen room type; `guests` must not exceed the room type's capacity.
- Single currency, no taxes; money is `BigDecimal` at 2 decimals.
- Check-in may be today or later; check-out must be after check-in; dates are interpreted in the server's time zone.
- A pending booking holds its room until it is paid, fails, or is cancelled.
- No authentication or authorisation (out of scope); guests are identified only by `guestName`.

## With more time

- Expire unpaid `PENDING_PAYMENT` holds after a timeout (scheduled job).
- Real persistence (JPA) with optimistic locking / conditional updates for inventory; pagination for search.
- Persist and reuse idempotency keys with a TTL and a request fingerprint check, so a reused key with a different payload is rejected.
- Dynamic pricing driven by occupancy and seasons; per-property cancellation policies.
- Publish booking events (Kafka) and add owner-facing inventory management (block dates, change room counts).
