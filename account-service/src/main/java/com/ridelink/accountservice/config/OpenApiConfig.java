package com.ridelink.accountservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

// Swagger page: title, how to use, the endpoint groups and the Authorize button
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideLink Account Service API",
                version = "v1",
                description = """
                        Registration, login and account management. This is the only service that issues tokens.

                        **How to use**
                        1. Register a PASSENGER or DRIVER account.
                        2. Log in and copy the accessToken.
                        3. Click Authorize and paste it. The same token works in the Driver, Ride and Fare services.
                        """,
                contact = @Contact(name = "RideLink Backend Team")),
        tags = {
                @Tag(name = "1. Authentication", description = "Register and log in (no token needed)"),
                @Tag(name = "2. My account", description = "View and update your own profile"),
                @Tag(name = "3. Admin", description = "Admin only: view accounts and change their status")
        },
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
