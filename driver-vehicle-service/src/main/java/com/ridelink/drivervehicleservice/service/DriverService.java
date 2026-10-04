package com.ridelink.drivervehicleservice.service;

import com.ridelink.drivervehicleservice.dto.AvailableDriverResponse;
import com.ridelink.drivervehicleservice.dto.CreateDriverRequest;
import com.ridelink.drivervehicleservice.dto.DriverResponse;
import com.ridelink.drivervehicleservice.exception.ApiException;
import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.DriverProfile;
import com.ridelink.drivervehicleservice.model.VehicleType;
import com.ridelink.drivervehicleservice.repository.DriverProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

// Business rules for driver profiles and availability
@Service
public class DriverService {

    private final DriverProfileRepository driverRepository;

    public DriverService(DriverProfileRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // Driver creates their profile (one per account) -> starts OFFLINE
    public DriverResponse createProfile(String accountId, String fullName, CreateDriverRequest request) {
        if (driverRepository.existsByAccountId(accountId)) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a driver profile");
        }
        if (driverRepository.existsByVehiclePlateNumber(request.vehicle().plateNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "This plate number is already registered");
        }
        DriverProfile driver = new DriverProfile();
        driver.setAccountId(accountId);
        driver.setFullName(fullName);
        driver.setLicenceNumber(request.licenceNumber().trim());
        driver.setServiceArea(request.serviceArea().trim());
        driver.setCurrentLocation(request.serviceArea().trim());   // starts in their service area
        driver.setVehicle(request.vehicle());
        driver.setAvailability(AvailabilityStatus.OFFLINE);
        driver.setCreatedAt(Instant.now());
        driver.setUpdatedAt(Instant.now());
        return DriverResponse.from(driverRepository.save(driver));
    }

    // The driver's own profile
    public DriverResponse getMyProfile(String accountId) {
        return DriverResponse.from(findByAccount(accountId));
    }

    // Driver updates licence, service area and vehicle (not while on a ride)
    public DriverResponse updateMyProfile(String accountId, CreateDriverRequest request) {
        DriverProfile driver = findByAccount(accountId);
        if (driver.getAvailability() == AvailabilityStatus.BUSY) {
            throw new ApiException(HttpStatus.CONFLICT, "You cannot change your profile during a ride");
        }
        boolean newPlate = !driver.getVehicle().plateNumber().equals(request.vehicle().plateNumber());
        if (newPlate && driverRepository.existsByVehiclePlateNumber(request.vehicle().plateNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "This plate number is already registered");
        }
        driver.setLicenceNumber(request.licenceNumber().trim());
        driver.setServiceArea(request.serviceArea().trim());
        driver.setVehicle(request.vehicle());
        driver.setUpdatedAt(Instant.now());
        return DriverResponse.from(driverRepository.save(driver));
    }

    // Driver updates their simulated current location
    public DriverResponse updateMyLocation(String accountId, String placeName) {
        DriverProfile driver = findByAccount(accountId);
        driver.setCurrentLocation(placeName.trim());
        driver.setUpdatedAt(Instant.now());
        return DriverResponse.from(driverRepository.save(driver));
    }

    // Driver goes online (AVAILABLE) or offline (OFFLINE). Only the Ride service can set BUSY.
    public DriverResponse setMyAvailability(String accountId, AvailabilityStatus status) {
        if (status == AvailabilityStatus.BUSY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only AVAILABLE or OFFLINE can be set by the driver");
        }
        DriverProfile driver = findByAccount(accountId);
        if (driver.getAvailability() == AvailabilityStatus.BUSY) {
            throw new ApiException(HttpStatus.CONFLICT, "You are on a ride; it must be completed or cancelled first");
        }
        changeAvailability(driver, status);
        return DriverResponse.from(driverRepository.save(driver));
    }

    // Search used by the Ride service: AVAILABLE drivers in the area with the right vehicle, longest-waiting first
    public List<AvailableDriverResponse> findAvailable(String area, VehicleType vehicleType) {
        return driverRepository
                .findByAvailabilityAndServiceAreaIgnoreCaseAndVehicleTypeOrderByAvailableSinceAsc(
                        AvailabilityStatus.AVAILABLE, area.trim(), vehicleType)
                .stream().map(AvailableDriverResponse::from).toList();
    }

    // Internal (Ride service): BUSY when assigned, AVAILABLE when the ride ends
    public DriverResponse setAvailabilityInternal(String driverId, AvailabilityStatus status) {
        DriverProfile driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found"));
        if (status == AvailabilityStatus.BUSY && driver.getAvailability() != AvailabilityStatus.AVAILABLE) {
            // stops two rides grabbing the same driver
            throw new ApiException(HttpStatus.CONFLICT, "Driver is not AVAILABLE");
        }
        changeAvailability(driver, status);
        return DriverResponse.from(driverRepository.save(driver));
    }

    // Admin: every driver
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream().map(DriverResponse::from).toList();
    }

    // ---------- helpers ----------

    private DriverProfile findByAccount(String accountId) {
        return driverRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver profile not found; create it first"));
    }

    private void changeAvailability(DriverProfile driver, AvailabilityStatus status) {
        if (status == AvailabilityStatus.AVAILABLE && driver.getAvailability() != AvailabilityStatus.AVAILABLE) {
            driver.setAvailableSince(Instant.now());   // start of the waiting time
        }
        driver.setAvailability(status);
        driver.setUpdatedAt(Instant.now());
    }
}
