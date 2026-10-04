package com.ridelink.drivervehicleservice.dto;

import com.ridelink.drivervehicleservice.model.DriverProfile;
import com.ridelink.drivervehicleservice.model.VehicleType;

// CONTRACT with the Ride service: Ride reads driverId (to call us back) and accountId (to check the
// driver's token). The other fields are for display only.
public record AvailableDriverResponse(String driverId, String accountId, String fullName,
                                      VehicleType vehicleType, String plateNumber, String serviceArea) {

    public static AvailableDriverResponse from(DriverProfile d) {
        return new AvailableDriverResponse(d.getId(), d.getAccountId(), d.getFullName(),
                d.getVehicle().type(), d.getVehicle().plateNumber(), d.getServiceArea());
    }
}
