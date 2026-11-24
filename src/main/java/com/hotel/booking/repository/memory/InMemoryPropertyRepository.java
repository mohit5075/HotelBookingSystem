package com.hotel.booking.repository.memory;

import com.hotel.booking.model.Property;
import com.hotel.booking.repository.PropertyRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPropertyRepository implements PropertyRepository {

    private final Map<String, Property> properties = new ConcurrentHashMap<>();

    @Override
    public Property save(Property property) {
        properties.put(property.id(), property);
        return property;
    }

    @Override
    public Optional<Property> findById(String id) {
        return Optional.ofNullable(properties.get(id));
    }

    @Override
    public List<Property> findAll() {
        return List.copyOf(properties.values());
    }

    @Override
    public List<Property> findByOwnerId(String ownerId) {
        return properties.values().stream().filter(p -> p.ownerId().equals(ownerId)).toList();
    }
}
