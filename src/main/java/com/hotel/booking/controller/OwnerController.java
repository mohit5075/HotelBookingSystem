package com.hotel.booking.controller;

import com.hotel.booking.dto.AddPropertyRequest;
import com.hotel.booking.dto.CreateOwnerRequest;
import com.hotel.booking.dto.OwnerResponse;
import com.hotel.booking.dto.PropertyResponse;
import com.hotel.booking.service.OwnerService;
import com.hotel.booking.service.PropertyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;
    private final PropertyService propertyService;

    public OwnerController(OwnerService ownerService, PropertyService propertyService) {
        this.ownerService = ownerService;
        this.propertyService = propertyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerResponse createOwner(@Valid @RequestBody CreateOwnerRequest request) {
        return OwnerResponse.from(ownerService.create(request.name()));
    }

    @PostMapping("/{ownerId}/properties")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse addProperty(@PathVariable String ownerId,
                                        @Valid @RequestBody AddPropertyRequest request) {
        return PropertyResponse.from(propertyService.register(request.toDomain(ownerId)));
    }

    @GetMapping("/{ownerId}/properties")
    public List<PropertyResponse> listProperties(@PathVariable String ownerId) {
        return propertyService.listByOwner(ownerId).stream().map(PropertyResponse::from).toList();
    }
}
