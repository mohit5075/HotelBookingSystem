# Hotel Booking Platform (backend)

Spring Boot 3 / Java 21 service for property discovery, onboarding, booking, payment and cancellation.
All data is in memory behind repository interfaces.

## Build and run

```
mvn clean test              # build + 33 unit/integration tests
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

Instruments: card = token `tok_...`, UPI = `name@bank`, wallet = any id. The mock gateway declines any instrument containing `decline`.
Errors use RFC 7807 problem details: 400 validation, 404 not found, 409 conflict (sold out, invalid state, idempotency).

## Key design decisions

- **Owner -> Property -> RoomType.** A standalone host is simply an owner with one property; there is no separate single-property path.
- **Search filters are Strategy beans** (`SearchFilter`). `SearchService` applies every bean it is given, so a new filter is one new class and nothing else changes. Availability and capacity are filters too.
- **Inventory** is tracked per room type per night (`InventoryRepository`). `tryReserve` is an atomic check-and-hold across all nights of the stay, which prevents double-booking; the check-out day is not held (back-to-back stays are allowed). A concurrency test books 20 simultaneous requests against 3 rooms and asserts exactly 3 succeed.
- **Booking lifecycle** (state machine in `BookingStatus`/`Booking`): `PENDING_PAYMENT -> CONFIRMED -> CANCELLED`, `PENDING_PAYMENT -> PAYMENT_FAILED | CANCELLED`. Illegal transitions throw `InvalidBookingStateException`. A booking holds inventory from creation; failure or cancellation releases it exactly once.
- **Payments:** `PaymentProcessor` per method (Card/UPI/Wallet) resolved by `PaymentProcessorRegistry`; they call `PaymentGateway`, whose only implementation is `MockPaymentGateway`. Payment outcome drives booking state. **Idempotency:** the same `Idempotency-Key` returns the original payment without charging again; reusing a key for another booking is a 409. Payment and cancellation of one booking are serialised by `StripedLock`, so pay-vs-cancel cannot interleave.
- **Cancellation:** pluggable `RefundPolicy`; default `TimeBasedRefundPolicy` refunds 100% >= 48h before check-in, 50% >= 24h, else 0. Unpaid bookings cancel with no refund.
- **Pricing:** pluggable `PricingStrategy` (base price x nights by default; a weekend-surcharge strategy is included as a dynamic-pricing example).
- Time comes from an injected `Clock`, which keeps the date and refund rules testable.

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
