package com.hotel.booking.model;

public record Location(String city, String locality) {

    public Location {
        city = Checks.text(city, "city");
        locality = Checks.text(locality, "locality");
    }
}
