package com.hotel.booking.repository;

import com.hotel.booking.model.Owner;
import java.util.Optional;

public interface OwnerRepository {

    Owner save(Owner owner);

    Optional<Owner> findById(String id);
}
