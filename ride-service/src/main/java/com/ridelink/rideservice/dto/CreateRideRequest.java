package com.ridelink.rideservice.dto;

import com.ridelink.rideservice.model.Location;
import com.ridelink.rideservice.model.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// What a passenger sends to request a ride
public record CreateRideRequest(
        @NotNull @Valid Location pickup,
        @NotNull @Valid Location destination,
        @NotNull VehicleType vehicleType) {
}