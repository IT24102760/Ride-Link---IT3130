package com.ridelink.drivervehicleservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// The driver's simulated current location (a place name)
public record LocationRequest(
        @Schema(example = "Negombo") @NotBlank @Size(max = 60) String placeName) {
}
