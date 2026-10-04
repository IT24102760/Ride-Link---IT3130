package com.ridelink.drivervehicleservice.dto;

import com.ridelink.drivervehicleservice.model.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// What a driver sends to create (or update) their profile
public record CreateDriverRequest(

        @Schema(example = "B1234567")
        @NotBlank @Size(max = 20) String licenceNumber,

        @Schema(description = "Place name where the driver works; must match ride pickup places", example = "Negombo")
        @NotBlank @Size(max = 60) String serviceArea,

        @NotNull @Valid Vehicle vehicle) {
}
