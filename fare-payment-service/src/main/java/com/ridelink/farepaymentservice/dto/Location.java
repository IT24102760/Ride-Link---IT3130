package com.ridelink.farepaymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A pickup or destination place (place name only, same shape as the Ride service).
// Coordinates are looked up in our PlaceDirectory.
public record Location(
        @Schema(description = "A supported place name (see GET /api/fares/places)", example = "Negombo")
        @NotBlank @Size(max = 60) String placeName) {
}
