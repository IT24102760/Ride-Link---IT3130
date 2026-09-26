package com.ridelink.rideservice.model;

import jakarta.validation.constraints.*;

// A pickup or destination point (simulated location)
public record Location(
        @NotBlank String placeName,                                  // e.g. "Negombo", also used as service area
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
}

