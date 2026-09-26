package com.ridelink.rideservice.dto;

import jakarta.validation.constraints.NotBlank;

// What is sent to cancel a ride
public record CancelRideRequest(@NotBlank String reason) {
}