package com.ridelink.rideservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Optional body for assigning a driver: leave it out to get the longest-waiting driver
public record AssignDriverRequest(
        @Schema(description = "A driverId from GET /api/rides/{rideId}/drivers (optional)")
        String driverId) {
}
