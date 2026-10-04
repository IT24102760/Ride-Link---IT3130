package com.ridelink.farepaymentservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

// Swagger page: title, fare rule, payment flow, endpoint groups and the Authorize button
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideLink Fare & Payment Service API",
                version = "v1",
                description = """
                        Fare estimates, final fares, simulated payments and receipts.

                        **Fare rule (LKR):** distance = straight-line distance x 1.3; \
                        fare = base + distance x per-km + minutes x per-minute, never below the minimum.
                        BIKE 100 + 40/km + 2/min (min 120) · TUK 150 + 50/km + 3/min (min 180) · \
                        CAR 200 + 70/km + 4/min (min 250).

                        **Payment flow**
                        1. The Ride service creates a PENDING payment when a ride is completed.
                        2. The passenger pays: CARD/MOBILE → PAID (or FAILED with simulateOutcome = FAIL, then pay again);
                           CASH → AWAITING_DRIVER_CONFIRMATION.
                        3. For cash, the assigned driver confirms → PAID.
                        4. The receipt is available once PAID.
                        """,
                contact = @Contact(name = "RideLink Backend Team")),
        tags = {
                @Tag(name = "1. Fare", description = "Supported places and fare estimates (any logged-in user)"),
                @Tag(name = "2. Passenger", description = "Pay for a completed ride (PASSENGER token)"),
                @Tag(name = "3. Driver", description = "Confirm a cash payment (the ride's DRIVER token)"),
                @Tag(name = "4. Payments and receipts", description = "View payments, history and receipts (passenger, driver or admin)"),
                @Tag(name = "5. Internal", description = "Called only by the Ride service (X-Service-Key header, no token)")
        },
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}

