package com.ridelink.rideservice.dto;

// Body sent to the Driver service to set a driver BUSY or AVAILABLE
public record AvailabilityUpdate(String status) {
}