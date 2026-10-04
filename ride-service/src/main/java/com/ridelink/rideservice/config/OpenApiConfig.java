package com.ridelink.rideservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

// Swagger page: title, ride flow, the three endpoint groups and the Authorize button
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideLink Ride Management Service API",
                version = "v1",
                description = """
                        Ride requests, driver assignment and the ride lifecycle.
                        """,
                contact = @Contact(name = "RideLink Backend Team")),
        tags = {
                @Tag(name = "1. Passenger",
                        description = "Passenger: book a ride and find a driver (use the PASSENGER token)"),
                @Tag(name = "2. Driver",
                        description = "Assigned driver: accept, start and complete the ride (use the DRIVER token)"),
                @Tag(name = "3. Passenger & Driver",
                        description = "Either person on the ride: cancel and view rides")
        },
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}