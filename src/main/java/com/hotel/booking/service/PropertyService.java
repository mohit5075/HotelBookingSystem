package com.hotel.booking.service;

import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.model.Property;
import com.hotel.booking.repository.PropertyRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerService ownerService;

    public PropertyService(PropertyRepository propertyRepository, OwnerService ownerService) {
        this.propertyRepository = propertyRepository;
        this.ownerService = ownerService;
    }

    public Property register(Property property) {
        ownerService.get(property.ownerId());
        return propertyRepository.save(property);
    }

    public Property get(String propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));
    }

    public List<Property> listByOwner(String ownerId) {
        ownerService.get(ownerId);
        return propertyRepository.findByOwnerId(ownerId);
    }
}
