package com.ridelink.accountservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Email and password for logging in
public record LoginRequest(
        @Schema(example = "nimal@ridelink.test") @NotBlank @Email String email,
        @Schema(example = "Passenger@123") @NotBlank String password) {
}
