package com.ridelink.drivervehicleservice.dto;

import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.DriverProfile;
import com.ridelink.drivervehicleservice.model.Vehicle;

import java.time.Instant;

// Full driver profile returned to the driver or an admin
public record DriverResponse(String driverId, String accountId, String fullName, String licenceNumber,
                             String serviceArea, String currentLocation, Vehicle vehicle,
                             AvailabilityStatus availability, Instant availableSince) {

    public static DriverResponse from(DriverProfile d) {
        return new DriverResponse(d.getId(), d.getAccountId(), d.getFullName(), d.getLicenceNumber(),
                d.getServiceArea(), d.getCurrentLocation(), d.getVehicle(),
                d.getAvailability(), d.getAvailableSince());
    }
}
