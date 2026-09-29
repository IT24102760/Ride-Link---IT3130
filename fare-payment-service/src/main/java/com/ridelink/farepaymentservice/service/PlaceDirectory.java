package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Our place table: supported place names and their (simulated) coordinates.
// Other services only send place names; we look up the coordinates here.
@Component
public class PlaceDirectory {

    public record Place(String name, double latitude, double longitude) { }

    private final Map<String, Place> places = new TreeMap<>();   // key = lower-case name

    public PlaceDirectory() {
        add("Colombo", 6.9271, 79.8612);
        add("Negombo", 7.2083, 79.8358);
        add("Katunayake", 7.1697, 79.8883);
        add("Ja-Ela", 7.0744, 79.8919);
        add("Wattala", 6.9897, 79.8918);
        add("Gampaha", 7.0917, 79.9999);
        add("Dehiwala", 6.8511, 79.8659);
        add("Nugegoda", 6.8649, 79.8997);
        add("Moratuwa", 6.7730, 79.8816);
        add("Kalutara", 6.5854, 79.9607);
        add("Kandy", 7.2906, 80.6337);
        add("Kurunegala", 7.4863, 80.3647);
        add("Chilaw", 7.5758, 79.7953);
        add("Galle", 6.0535, 80.2210);
        add("Matara", 5.9549, 80.5550);
        add("Anuradhapura", 8.3114, 80.4037);
        add("Jaffna", 9.6615, 80.0255);
        add("Trincomalee", 8.5874, 81.2152);
        add("Nuwara Eliya", 6.9497, 80.7891);
        add("Ratnapura", 6.6828, 80.3992);
    }

    private void add(String name, double latitude, double longitude) {
        places.put(name.toLowerCase(), new Place(name, latitude, longitude));
    }

    // Finds a place by name (ignoring case and extra spaces); unknown places give 400
    public Place find(String placeName) {
        Place place = places.get(placeName.trim().toLowerCase());
        if (place == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Unknown place: " + placeName + ". See GET /api/fares/places for supported places");
        }
        return place;
    }

    public List<String> allNames() {
        return places.values().stream().map(Place::name).toList();
    }
}