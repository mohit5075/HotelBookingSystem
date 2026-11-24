package com.hotel.booking.repository.memory;

import com.hotel.booking.model.Owner;
import com.hotel.booking.repository.OwnerRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOwnerRepository implements OwnerRepository {

    private final Map<String, Owner> owners = new ConcurrentHashMap<>();

    @Override
    public Owner save(Owner owner) {
        owners.put(owner.id(), owner);
        return owner;
    }

    @Override
    public Optional<Owner> findById(String id) {
        return Optional.ofNullable(owners.get(id));
    }
}
