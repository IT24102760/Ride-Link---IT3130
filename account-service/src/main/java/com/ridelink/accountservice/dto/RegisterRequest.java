package com.ridelink.accountservice.dto;

import com.ridelink.accountservice.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// What a new passenger or driver sends to create an account
public record RegisterRequest(

        @Schema(example = "Nimal Perera")
        @NotBlank @Size(max = 80) String fullName,

        @Schema(example = "nimal@ridelink.test")
        @NotBlank @Email String email,

        @Schema(description = "At least 8 characters", example = "Passenger@123")
        @NotBlank @Size(min = 8, max = 64) String password,

        @Schema(description = "Optional, digits with an optional +", example = "+94771234567")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "must be 9 to 15 digits") String phone,

        @Schema(description = "PASSENGER or DRIVER (ADMIN cannot self-register)", example = "PASSENGER")
        @NotNull Role role) {
}
