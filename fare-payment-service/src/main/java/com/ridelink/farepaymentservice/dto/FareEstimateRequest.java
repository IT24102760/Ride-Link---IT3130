package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// What a passenger sends to get a price before booking
public record FareEstimateRequest(
        @NotNull @Valid Location pickup,
        @NotNull @Valid Location destination,
        @Schema(example = "CAR") @NotNull VehicleType vehicleType) {
}
