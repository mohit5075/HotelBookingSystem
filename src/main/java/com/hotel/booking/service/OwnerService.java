package com.hotel.booking.service;

import com.hotel.booking.exception.ResourceNotFoundException;
import com.hotel.booking.model.Owner;
import com.hotel.booking.repository.OwnerRepository;
import org.springframework.stereotype.Service;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public Owner create(String name) {
        return ownerRepository.save(Owner.create(name));
    }

    public Owner get(String ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner", ownerId));
    }
}
