package com.ridelink.rideservice.dto;

import com.ridelink.rideservice.model.Location;
import com.ridelink.rideservice.model.VehicleType;

// Body sent to POST /internal/payments when a ride is completed
public record CreatePaymentRequest(
        String rideId, String passengerId, String driverId, VehicleType vehicleType,
        Location pickup, Location destination, long tripMinutes) {
}