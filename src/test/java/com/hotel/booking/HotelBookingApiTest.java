package com.hotel.booking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HotelBookingApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private JsonNode send(org.springframework.test.web.servlet.RequestBuilder request, int expected)
            throws Exception {
        final String body = mvc.perform(request).andExpect(status().is(expected)).andReturn().getResponse()
                .getContentAsString();
        return mapper.readTree(body);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder json(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
            String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void bookingFlow() throws Exception {
        final JsonNode owner = send(json(post("/api/owners"), "{\"name\":\"Lakeview Hotels\"}"), 201);
        final String ownerId = owner.get("id").asText();

        final JsonNode property = send(json(post("/api/owners/" + ownerId + "/properties"), """
                {"name":"Lakeview Palace","city":"Udaipur","locality":"Pichola","starRating":5,
                 "amenities":["WIFI","POOL"],
                 "roomTypes":[{"name":"Suite","capacity":3,"basePrice":150.00,"totalRooms":1}]}
                """), 201);
        final String propertyId = property.get("id").asText();
        final String roomTypeId = property.get("roomTypes").get(0).get("id").asText();

        final LocalDate checkIn = LocalDate.now().plusDays(30);
        final LocalDate checkOut = checkIn.plusDays(2);
        final String searchUrl = "/api/properties/search?city=Udaipur&guests=2&amenities=POOL&checkIn=" + checkIn
                + "&checkOut=" + checkOut;
        mvc.perform(get(searchUrl)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].propertyId").value(propertyId))
                .andExpect(jsonPath("$[0].availableRooms[0].totalPrice").value(300.0));

        final String bookingBody = """
                {"propertyId":"%s","roomTypeId":"%s","guestName":"Rohan","checkIn":"%s","checkOut":"%s","guests":2}
                """.formatted(propertyId, roomTypeId, checkIn, checkOut);
        final JsonNode booking = send(json(post("/api/bookings"), bookingBody), 201);
        final String bookingId = booking.get("id").asText();

        send(json(post("/api/bookings"), bookingBody), 409);
        mvc.perform(get(searchUrl)).andExpect(jsonPath("$").isEmpty());

        final String payBody = "{\"method\":\"UPI\",\"instrument\":\"rohan@okbank\"}";
        mvc.perform(json(post("/api/bookings/" + bookingId + "/payments").header("Idempotency-Key", "abc"), payBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS"));
        mvc.perform(get("/api/bookings/" + bookingId)).andExpect(jsonPath("$.status").value("CONFIRMED"));

        mvc.perform(post("/api/bookings/" + bookingId + "/cancel")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(300.0));
        mvc.perform(get(searchUrl)).andExpect(jsonPath("$[0].propertyId").value(propertyId));
        send(post("/api/bookings/" + bookingId + "/cancel"), 409);
    }

    @Test
    void errorStatusCodes() throws Exception {
        send(json(post("/api/owners"), "{\"name\":\"\"}"), 400);
        send(get("/api/properties/unknown"), 404);
        send(get("/api/properties/search?guests=2"), 400);
        send(json(post("/api/bookings/unknown/payments"), "{\"method\":\"CARD\",\"instrument\":\"tok_a\"}"), 400);
    }
}
