package com.ridelink.farepaymentservice.controller;

import com.ridelink.farepaymentservice.dto.FareEstimateRequest;
import com.ridelink.farepaymentservice.model.FareBreakdown;
import com.ridelink.farepaymentservice.service.FareCalculator;
import com.ridelink.farepaymentservice.service.PlaceDirectory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Fare endpoints (any logged-in user)
@RestController
@RequestMapping("/api/fares")
@Tag(name = "1. Fare")
public class FareController {

    private final FareCalculator fareCalculator;
    private final PlaceDirectory placeDirectory;

    public FareController(FareCalculator fareCalculator, PlaceDirectory placeDirectory) {
        this.fareCalculator = fareCalculator;
        this.placeDirectory = placeDirectory;
    }

    @Operation(summary = "List the supported place names",
            description = "Use these names for ride pickup/destination and for drivers' service areas.")
    @GetMapping("/places")
    public List<String> getPlaces() {
        return placeDirectory.allNames();
    }

    @Operation(summary = "Estimate a fare before booking",
            description = "The trip time is estimated from the distance at an average of 30 km/h. Nothing is saved.")
    @ApiResponse(responseCode = "200", description = "Estimated fare with its breakdown")
    @ApiResponse(responseCode = "400", description = "Invalid input or unknown place")
    @PostMapping("/estimate")
    public FareBreakdown estimate(@Valid @RequestBody FareEstimateRequest request) {
        return fareCalculator.estimate(request.vehicleType(),
                request.pickup().placeName(), request.destination().placeName());
    }
}