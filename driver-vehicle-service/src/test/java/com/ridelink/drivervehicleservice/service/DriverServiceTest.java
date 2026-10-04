package com.ridelink.drivervehicleservice.service;

import com.ridelink.drivervehicleservice.dto.AvailableDriverResponse;
import com.ridelink.drivervehicleservice.dto.CreateDriverRequest;
import com.ridelink.drivervehicleservice.dto.DriverResponse;
import com.ridelink.drivervehicleservice.exception.ApiException;
import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.DriverProfile;
import com.ridelink.drivervehicleservice.model.Vehicle;
import com.ridelink.drivervehicleservice.model.VehicleType;
import com.ridelink.drivervehicleservice.repository.DriverProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Tests DriverService with a fake (mocked) repository - no database needed
@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock DriverProfileRepository driverRepository;
    @InjectMocks DriverService driverService;

    private static final String KASUN = "acc-2";
    private static final Vehicle CAR = new Vehicle("CAB-1234", VehicleType.CAR, "Toyota Axio", "White");
    private static final CreateDriverRequest PROFILE = new CreateDriverRequest("B1234567", "Negombo", CAR);

    // A stored driver profile with the given availability
    private DriverProfile driver(AvailabilityStatus availability) {
        DriverProfile d = new DriverProfile();
        d.setId("drv-7");
        d.setAccountId(KASUN);
        d.setFullName("Kasun Silva");
        d.setServiceArea("Negombo");
        d.setVehicle(CAR);
        d.setAvailability(availability);
        return d;
    }

    // Makes the fake repository return whatever is saved
    private void saveReturnsInput() {
        when(driverRepository.save(any(DriverProfile.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ----- profile -----

    @Test
    void createProfile_startsOfflineInServiceArea() {
        when(driverRepository.existsByAccountId(KASUN)).thenReturn(false);
        when(driverRepository.existsByVehiclePlateNumber("CAB-1234")).thenReturn(false);
        saveReturnsInput();

        DriverResponse result = driverService.createProfile(KASUN, "Kasun Silva", PROFILE);

        assertEquals(KASUN, result.accountId());                  // linked to the token's sub
        assertEquals(AvailabilityStatus.OFFLINE, result.availability());
        assertEquals("Negombo", result.currentLocation());
    }

    @Test
    void createProfile_twice_isConflict() {
        when(driverRepository.existsByAccountId(KASUN)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> driverService.createProfile(KASUN, "Kasun Silva", PROFILE));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void createProfile_plateAlreadyUsed_isConflict() {
        when(driverRepository.existsByAccountId(KASUN)).thenReturn(false);
        when(driverRepository.existsByVehiclePlateNumber("CAB-1234")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> driverService.createProfile(KASUN, "Kasun Silva", PROFILE));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void getMyProfile_notCreated_isNotFound() {
        when(driverRepository.findByAccountId(KASUN)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> driverService.getMyProfile(KASUN));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    // ----- driver availability -----

    @Test
    void goOnline_becomesAvailableWithWaitingTime() {
        when(driverRepository.findByAccountId(KASUN)).thenReturn(Optional.of(driver(AvailabilityStatus.OFFLINE)));
        saveReturnsInput();

        DriverResponse result = driverService.setMyAvailability(KASUN, AvailabilityStatus.AVAILABLE);

        assertEquals(AvailabilityStatus.AVAILABLE, result.availability());
        assertNotNull(result.availableSince());
    }

    @Test
    void driverSettingBusy_isBadRequest() {
        ApiException ex = assertThrows(ApiException.class,
                () -> driverService.setMyAvailability(KASUN, AvailabilityStatus.BUSY));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verifyNoInteractions(driverRepository);
    }

    @Test
    void goOffline_whileBusy_isConflict() {
        when(driverRepository.findByAccountId(KASUN)).thenReturn(Optional.of(driver(AvailabilityStatus.BUSY)));

        ApiException ex = assertThrows(ApiException.class,
                () -> driverService.setMyAvailability(KASUN, AvailabilityStatus.OFFLINE));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void updateProfile_whileBusy_isConflict() {
        when(driverRepository.findByAccountId(KASUN)).thenReturn(Optional.of(driver(AvailabilityStatus.BUSY)));

        ApiException ex = assertThrows(ApiException.class, () -> driverService.updateMyProfile(KASUN, PROFILE));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    // ----- search (used by the Ride service) -----

    @Test
    void findAvailable_returnsDriverIdAndAccountId() {
        when(driverRepository.findByAvailabilityAndServiceAreaIgnoreCaseAndVehicleTypeOrderByAvailableSinceAsc(
                AvailabilityStatus.AVAILABLE, "Negombo", VehicleType.CAR))
                .thenReturn(List.of(driver(AvailabilityStatus.AVAILABLE)));

        List<AvailableDriverResponse> result = driverService.findAvailable(" Negombo ", VehicleType.CAR);

        assertEquals(1, result.size());
        assertEquals("drv-7", result.get(0).driverId());   // Ride calls us back with this
        assertEquals(KASUN, result.get(0).accountId());    // Ride checks the driver's token with this
    }

    @Test
    void findAvailable_nobodyOnline_isEmptyList() {
        when(driverRepository.findByAvailabilityAndServiceAreaIgnoreCaseAndVehicleTypeOrderByAvailableSinceAsc(
                any(), any(), any())).thenReturn(List.of());

        assertTrue(driverService.findAvailable("Galle", VehicleType.TUK).isEmpty());
    }

    // ----- internal (called by the Ride service) -----

    @Test
    void internalBusy_whenAvailable_becomesBusy() {
        when(driverRepository.findById("drv-7")).thenReturn(Optional.of(driver(AvailabilityStatus.AVAILABLE)));
        saveReturnsInput();

        DriverResponse result = driverService.setAvailabilityInternal("drv-7", AvailabilityStatus.BUSY);

        assertEquals(AvailabilityStatus.BUSY, result.availability());
    }

    @Test
    void internalBusy_whenAlreadyBusy_isConflict() {
        when(driverRepository.findById("drv-7")).thenReturn(Optional.of(driver(AvailabilityStatus.BUSY)));

        ApiException ex = assertThrows(ApiException.class,
                () -> driverService.setAvailabilityInternal("drv-7", AvailabilityStatus.BUSY));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());   // Ride then tries the next driver
        verify(driverRepository, never()).save(any());
    }

    @Test
    void internalAvailable_afterRide_releasesDriver() {
        when(driverRepository.findById("drv-7")).thenReturn(Optional.of(driver(AvailabilityStatus.BUSY)));
        saveReturnsInput();

        DriverResponse result = driverService.setAvailabilityInternal("drv-7", AvailabilityStatus.AVAILABLE);

        assertEquals(AvailabilityStatus.AVAILABLE, result.availability());
        assertNotNull(result.availableSince());
    }

    @Test
    void internal_unknownDriver_isNotFound() {
        when(driverRepository.findById("missing")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> driverService.setAvailabilityInternal("missing", AvailabilityStatus.BUSY));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
