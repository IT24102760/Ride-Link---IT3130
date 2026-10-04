package com.ridelink.drivervehicleservice.dto;

import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// Driver: AVAILABLE or OFFLINE.  Ride service (internal): BUSY or AVAILABLE.
public record AvailabilityRequest(
        @Schema(example = "AVAILABLE") @NotNull AvailabilityStatus status) {
}
