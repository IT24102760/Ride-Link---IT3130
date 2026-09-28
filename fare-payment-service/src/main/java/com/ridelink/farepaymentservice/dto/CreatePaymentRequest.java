package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotBlank(message = "Ride ID is required")
        String rideId,

        @NotBlank(message = "Passenger ID is required")
        String passengerId,

        @NotBlank(message = "Driver ID is required")
        String driverId,

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

) {
}
