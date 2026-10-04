package com.ridelink.rideservice.client;

import com.ridelink.rideservice.dto.CreatePaymentRequest;
import com.ridelink.rideservice.dto.PaymentResult;
import com.ridelink.rideservice.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// REST calls from the Ride service to the Fare & Payment service
@Component
public class FareServiceClient {

    private final RestClient restClient;
    private final String serviceKey;

    public FareServiceClient(@Value("${services.fare.url}") String baseUrl,
                             @Value("${app.internal.service-key}") String serviceKey,
                             ClientHttpRequestFactory requestFactory) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.serviceKey = serviceKey;
    }

    // POST /internal/payments - Fare calculates the final fare and creates a PENDING payment
    public PaymentResult createPayment(CreatePaymentRequest request) {
        try {
            PaymentResult result = restClient.post()
                    .uri("/internal/payments")
                    .header("X-Service-Key", serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PaymentResult.class);
            if (result == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Fare service returned an empty response");
            }
            return result;
        } catch (RestClientResponseException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Fare service returned " + ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Fare service is unavailable");
        }
    }
}