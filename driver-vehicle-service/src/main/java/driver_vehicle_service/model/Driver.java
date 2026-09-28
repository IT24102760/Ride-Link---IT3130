package driver_vehicle_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    private String accountId;

    private String serviceArea;

    private VehicleType vehicleType;

    private DriverAvailability availability;

    public Driver() {
    }

    public Driver(String accountId,
                  String serviceArea,
                  VehicleType vehicleType,
                  DriverAvailability availability) {
        this.accountId = accountId;
        this.serviceArea = serviceArea;
        this.vehicleType = vehicleType;
        this.availability = availability;
    }

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public DriverAvailability getAvailability() {
        return availability;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setAvailability(DriverAvailability availability) {
        this.availability = availability;
    }
}