package com.ridelink.drivervehicleservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

// Swagger page: title, driver flow, the endpoint groups and the Authorize button
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideLink Driver & Vehicle Service API",
                version = "v1",
                description = """
                        Driver profiles, vehicles, service area, simulated location and availability.

                        **Driver flow**
                        1. Log in at the Account service as a DRIVER and click Authorize here.
                        2. Create your profile with your vehicle → OFFLINE.
                        3. Go online: set availability to AVAILABLE.
                        4. The Ride service finds AVAILABLE drivers in the pickup area and marks one BUSY.
                        5. When the ride is completed or cancelled, the Ride service sets the driver AVAILABLE again.

                        **Matching rule:** only AVAILABLE drivers whose service area matches the pickup place
                        and whose vehicle type matches are returned, longest-waiting driver first.
                        """,
                contact = @Contact(name = "RideLink Backend Team")),
        tags = {
                @Tag(name = "1. Driver", description = "Driver: profile, vehicle, location and going online (DRIVER token)"),
                @Tag(name = "2. Available drivers", description = "Search AVAILABLE drivers (any logged-in user; used by the Ride service)"),
                @Tag(name = "3. Internal", description = "Called only by the Ride service (X-Service-Key header, no token)"),
                @Tag(name = "4. Admin", description = "Admin only: view all drivers")
        },
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
