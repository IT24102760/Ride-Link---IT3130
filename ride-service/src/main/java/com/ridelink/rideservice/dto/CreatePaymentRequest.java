package com.ridelink.rideservice.dto;

import com.ridelink.rideservice.model.Location;
import com.ridelink.rideservice.model.VehicleType;

// Body sent to POST /internal/payments when a ride is completed
public record CreatePaymentRequest(
        String rideId,
        String passengerId,
        String driverId,          // driver profile id (Driver service)
        String driverAccountId,   // driver's account id, lets Fare check the driver's token
        VehicleType vehicleType,
        Location pickup,          // place name only
        Location destination,     // place name only
        long tripMinutes) {
}