package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PayRequest(

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String simulateOutcome

) {
}
