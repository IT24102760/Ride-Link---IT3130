package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// CONTRACT with the Ride service: body of POST /internal/payments when a ride is completed
public record CreatePaymentRequest(
        @NotBlank String rideId,
        @NotBlank String passengerId,       // passenger's account id
        @NotBlank String driverId,          // driver profile id
        @NotBlank String driverAccountId,   // driver's account id (for cash confirmation)
        @NotNull VehicleType vehicleType,
        @NotNull @Valid Location pickup,
        @NotNull @Valid Location destination,
        @Min(1) long tripMinutes) {
}

