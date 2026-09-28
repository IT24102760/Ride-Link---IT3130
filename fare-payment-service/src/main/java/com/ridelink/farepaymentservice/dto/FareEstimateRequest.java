package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FareEstimateRequest(

        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType,

        @Valid
        @NotNull(message = "Pickup location is required")
        Location pickup,

        @Valid
        @NotNull(message = "Destination location is required")
        Location destination,

        @Min(value = 1, message = "Trip minutes must be at least 1")
        long tripMinutes

){

}
