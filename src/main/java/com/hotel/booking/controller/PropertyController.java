package com.hotel.booking.controller;

import com.hotel.booking.dto.PropertyResponse;
import com.hotel.booking.dto.PropertySearchResponse;
import com.hotel.booking.dto.SearchRequest;
import com.hotel.booking.service.PropertyService;
import com.hotel.booking.service.SearchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final SearchService searchService;
    private final PropertyService propertyService;

    public PropertyController(SearchService searchService, PropertyService propertyService) {
        this.searchService = searchService;
        this.propertyService = propertyService;
    }

    @GetMapping("/search")
    public List<PropertySearchResponse> search(@Valid SearchRequest request) {
        return searchService.search(request.toQuery()).stream().map(PropertySearchResponse::from).toList();
    }

    @GetMapping("/{propertyId}")
    public PropertyResponse get(@PathVariable String propertyId) {
        return PropertyResponse.from(propertyService.get(propertyId));
    }
}
