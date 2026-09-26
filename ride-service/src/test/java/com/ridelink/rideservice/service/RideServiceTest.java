package com.ridelink.rideservice.service;

import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.dto.RideResponse;
import com.ridelink.rideservice.exception.ApiException;
import com.ridelink.rideservice.model.Location;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.model.VehicleType;
import com.ridelink.rideservice.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Tests RideService with a fake (mocked) repository - no database needed
@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock RideRepository rideRepository;
    @InjectMocks RideService rideService;

    private static final String PASSENGER = "passenger-1";
    private static final String DRIVER = "driver-account-1";
    private static final Location NEGOMBO = new Location("Negombo", 7.2083, 79.8358);
    private static final Location COLOMBO = new Location("Colombo", 6.9271, 79.8612);

    // Builds a test ride in the given status (with a driver once assigned)
    private Ride ride(RideStatus status) {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId(PASSENGER);
        ride.setPickup(NEGOMBO);
        ride.setDestination(COLOMBO);
        ride.setVehicleType(VehicleType.CAR);
        ride.setStatus(status);
        if (status != RideStatus.REQUESTED) {
            ride.setDriverId("driver-profile-1");
            ride.setDriverAccountId(DRIVER);
        }
        return ride;
    }

    // Makes the fake repository return whatever is saved
    private void saveReturnsInput() {
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ----- normal behaviour -----

    @Test
    void requestRide_savesRideAsRequested() {
        saveReturnsInput();

        RideResponse result = rideService.requestRide(PASSENGER,
                new CreateRideRequest(NEGOMBO, COLOMBO, VehicleType.CAR));

        assertEquals(RideStatus.REQUESTED, result.status());
        assertEquals(PASSENGER, result.passengerId());
        assertNotNull(result.requestedAt());
    }

    @Test
    void acceptRide_byAssignedDriver_becomesAccepted() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.ASSIGNED)));
        saveReturnsInput();

        RideResponse result = rideService.acceptRide("ride-1", DRIVER);

        assertEquals(RideStatus.ACCEPTED, result.status());
    }

    @Test
    void startRide_setsInProgressAndStartTime() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.ACCEPTED)));
        saveReturnsInput();

        RideResponse result = rideService.startRide("ride-1", DRIVER);

        assertEquals(RideStatus.IN_PROGRESS, result.status());
        assertNotNull(result.startedAt());
    }

    @Test
    void cancelRide_byPassengerBeforeAssignment_becomesCancelled() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.REQUESTED)));
        saveReturnsInput();

        RideResponse result = rideService.cancelRide("ride-1", PASSENGER, "PASSENGER", "Changed plans");

        assertEquals(RideStatus.CANCELLED, result.status());
        assertEquals("Changed plans", result.cancelReason());
    }

    // ----- failure behaviour -----

    @Test
    void acceptRide_byWrongDriver_isForbidden() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.ASSIGNED)));

        ApiException ex = assertThrows(ApiException.class,
                () -> rideService.acceptRide("ride-1", "someone-else"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(rideRepository, never()).save(any());
    }

    @Test
    void startRide_twice_isConflict() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.IN_PROGRESS)));

        ApiException ex = assertThrows(ApiException.class,
                () -> rideService.startRide("ride-1", DRIVER));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(rideRepository, never()).save(any());
    }

    @Test
    void cancelRide_afterTripStarted_isConflict() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.IN_PROGRESS)));

        ApiException ex = assertThrows(ApiException.class,
                () -> rideService.cancelRide("ride-1", PASSENGER, "PASSENGER", "Too late"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void cancelRide_byStranger_isForbidden() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.REQUESTED)));

        ApiException ex = assertThrows(ApiException.class,
                () -> rideService.cancelRide("ride-1", "stranger", "PASSENGER", "Hack"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void getRide_unknownId_isNotFound() {
        when(rideRepository.findById("missing")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> rideService.getRide("missing", PASSENGER, "PASSENGER"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getRide_byAdmin_isAllowed() {
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride(RideStatus.REQUESTED)));

        RideResponse result = rideService.getRide("ride-1", "admin-1", "ADMIN");

        assertEquals("ride-1", result.rideId());
    }
}