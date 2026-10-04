package com.ridelink.accountservice.dto;

import com.ridelink.accountservice.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// Admin changes an account's status
public record UpdateStatusRequest(
        @Schema(description = "ACTIVE, SUSPENDED or DEACTIVATED", example = "SUSPENDED")
        @NotNull AccountStatus status) {
}
