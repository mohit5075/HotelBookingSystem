package com.hotel.booking.repository.memory;

import com.hotel.booking.model.DateRange;
import com.hotel.booking.repository.InventoryRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Keeps a count of rooms held per room type per night. The outer ConcurrentHashMap lets different
 * room types be handled in parallel; each room type's own map is guarded by a lock on itself, so the
 * check-and-hold in {@code tryReserve} cannot interleave with another request for the same room type.
 * That only works inside one JVM: with several application instances this needs a database-level
 * guarantee (for example a conditional UPDATE or a row lock).
 */
@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<String, Map<LocalDate, Integer>> heldByRoomType = new ConcurrentHashMap<>();

    @Override
    public boolean tryReserve(String roomTypeId, DateRange stay, int totalRooms) {
        Map<LocalDate, Integer> held = heldFor(roomTypeId);
        synchronized (held) {
            if (mostHeld(held, stay) >= totalRooms) {
                return false;
            }
            stay.dates().forEach(night -> held.merge(night, 1, Integer::sum));
            return true;
        }
    }

    @Override
    public void release(String roomTypeId, DateRange stay) {
        Map<LocalDate, Integer> held = heldFor(roomTypeId);
        synchronized (held) {
            stay.dates().forEach(night -> held.computeIfPresent(night, (n, count) -> count > 1 ? count - 1 : null));
        }
    }

    @Override
    public int availableRooms(String roomTypeId, DateRange stay, int totalRooms) {
        Map<LocalDate, Integer> held = heldFor(roomTypeId);
        synchronized (held) {
            return totalRooms - mostHeld(held, stay);
        }
    }

    private Map<LocalDate, Integer> heldFor(String roomTypeId) {
        return heldByRoomType.computeIfAbsent(roomTypeId, id -> new HashMap<>());
    }

    private int mostHeld(Map<LocalDate, Integer> held, DateRange stay) {
        return stay.dates().mapToInt(night -> held.getOrDefault(night, 0)).max().orElse(0);
    }
}
