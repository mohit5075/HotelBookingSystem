package com.hotel.booking.service;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Serialises work per key (e.g. booking id) using a fixed set of striped locks,
 * so payment and cancellation of the same booking can never interleave.
 */
@Component
public class StripedLock {

    private static final int STRIPES = 64;

    private final ReentrantLock[] locks = new ReentrantLock[STRIPES];

    public StripedLock() {
        for (int i = 0; i < STRIPES; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    public <T> T withLock(String key, Supplier<T> action) {
        final ReentrantLock lock = locks[Math.floorMod(key.hashCode(), STRIPES)];
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
