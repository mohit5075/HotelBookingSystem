package com.hotel.booking.repository;

import com.hotel.booking.model.DateRange;

/**
 * Per-night room inventory. Implementations must make {@link #tryReserve} atomic
 * (check-and-hold in one step), like a conditional UPDATE in a real database.
 */
public interface InventoryRepository {

    /** Holds one room for every night of the stay, or holds nothing and returns false. */
    boolean tryReserve(String roomTypeId, DateRange stay, int totalRooms);

    /** Gives back one room for every night of the stay. */
    void release(String roomTypeId, DateRange stay);

    /** Rooms still free for the whole stay (the minimum across its nights). */
    int availableRooms(String roomTypeId, DateRange stay, int totalRooms);
}
