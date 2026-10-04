package com.ridelink.drivervehicleservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// The driver's vehicle (stored inside the driver profile)
public record Vehicle(

        @Schema(example = "CAB-1234")
        @NotBlank @Pattern(regexp = "^[A-Z]{2,3}-\\d{4}$", message = "must look like CAB-1234") String plateNumber,

        @Schema(example = "CAR")
        @NotNull VehicleType type,

        @Schema(example = "Toyota Axio")
        @NotBlank @Size(max = 40) String model,

        @Schema(example = "White")
        @NotBlank @Size(max = 20) String colour) {
}
