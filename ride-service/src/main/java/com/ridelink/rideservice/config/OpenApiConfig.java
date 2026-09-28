package com.ridelink.rideservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideLink Ride Management Service API",
                version = "v1",
                description = """
                        RideLink Ride Management Service.

                        Provides ride request, driver assignment,
                        ride lifecycle, cancellation and retrieval operations.

                        Ride lifecycle:
                        REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED

                        Cancellation:
                        REQUESTED / ASSIGNED / ACCEPTED → CANCELLED
                        """,
                contact = @Contact(
                        name = "RideLink Backend Team"
                )
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}