package com.ridelink.rideservice.client;

import com.ridelink.rideservice.dto.AvailabilityUpdate;
import com.ridelink.rideservice.dto.AvailableDriver;
import com.ridelink.rideservice.exception.ApiException;
import com.ridelink.rideservice.model.VehicleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

// REST calls from the Ride service to the Driver & Vehicle service
@Component
public class DriverServiceClient {

    private final RestClient restClient;
    private final String serviceKey;

    public DriverServiceClient(@Value("${services.driver.url}") String baseUrl,
                               @Value("${app.internal.service-key}") String serviceKey,
                               ClientHttpRequestFactory requestFactory) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.serviceKey = serviceKey;
    }

    // GET /api/drivers/available - forwards the passenger's own token
    public List<AvailableDriver> findAvailableDrivers(String area, VehicleType vehicleType, String bearerToken) {
        try {
            List<AvailableDriver> drivers = restClient.get()
                    .uri(uri -> uri.path("/api/drivers/available")
                            .queryParam("area", area)
                            .queryParam("vehicleType", vehicleType)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AvailableDriver>>() {});
            return drivers == null ? List.of() : drivers;
        } catch (RestClientResponseException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Driver service returned " + ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Driver service is unavailable");
        }
    }

    // PATCH /internal/drivers/{id}/availability - protected by the shared service key
    public void setAvailability(String driverId, String status) {
        try {
            restClient.patch()
                    .uri("/internal/drivers/{driverId}/availability", driverId)
                    .header("X-Service-Key", serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AvailabilityUpdate(status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict ex) {
            // driver was taken by another ride at the same moment
            throw new ApiException(HttpStatus.CONFLICT, "Driver is no longer available");
        } catch (RestClientResponseException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Driver service returned " + ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Driver service is unavailable");
        }
    }
}