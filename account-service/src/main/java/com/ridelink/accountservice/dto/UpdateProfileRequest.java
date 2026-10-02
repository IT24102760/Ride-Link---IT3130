package com.ridelink.accountservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Fields a user can change on their own profile
public record UpdateProfileRequest(
        @Schema(example = "Nimal Perera") @NotBlank @Size(max = 80) String fullName,
        @Schema(example = "+94771234567")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "must be 9 to 15 digits") String phone) {
}
