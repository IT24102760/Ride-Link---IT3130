package com.ridelink.drivervehicleservice.model;

// OFFLINE = not working, AVAILABLE = online and free, BUSY = on a ride (set only by the Ride service)
public enum AvailabilityStatus {
    OFFLINE,
    AVAILABLE,
    BUSY
}
