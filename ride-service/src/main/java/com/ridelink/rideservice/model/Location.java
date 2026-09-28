package com.ridelink.rideservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A pickup or destination place. Coordinates are looked up by the Fare service.
@Schema(description = "A pickup or destination place (place name only)")
public record Location(
        @Schema(description = "Place name, also used as the driver's service area", example = "Negombo")
        @NotBlank @Size(max = 60) String placeName) {
}