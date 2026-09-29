package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// What the passenger sends to pay
public record PayRequest(
        @Schema(description = "CARD, MOBILE or CASH", example = "CARD")
        @NotNull PaymentMethod method,

        @Schema(description = "Simulation only (CARD/MOBILE): SUCCESS (default) or FAIL", example = "SUCCESS")
        String simulateOutcome) {
}
