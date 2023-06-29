package ro.ananimarius.allridev3.Common;

import java.sql.Timestamp;

public class WaypointDTO implements Cloneable{
    private Long id;
    private Timestamp time;
    private double destinationLatitude;
    private double destinationLongitude;
    private double customerLatitude;
    private double customerLongitude;
    private float direction;
    private String customerId;
    private String driverId;;
    public String getCustomerId() {
        return customerId;
    }

    public WaypointDTO() {
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }
    //</editor-fold>


    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public double getCustomerLatitude() {
        return customerLatitude;
    }

    public void setCustomerLatitude(double customerLatitude) {
        this.customerLatitude = customerLatitude;
    }

    public double getCustomerLongitude() {
        return customerLongitude;
    }

    public void setCustomerLongitude(double customerLongitude) {
        this.customerLongitude = customerLongitude;
    }


    public WaypointDTO(Long id, Timestamp time, double destinationLatitude, double destinationLongitude, double customerLatitude, double customerLongitude, float direction, String customerId, String driverId) {
        this.id = id;
        this.time = time;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.customerLatitude = customerLatitude;
        this.customerLongitude = customerLongitude;
        this.direction = direction;
        this.customerId = customerId;
        this.driverId = driverId;
    }

    @Override
    public WaypointDTO clone() {
        try {
            WaypointDTO clone = (WaypointDTO) super.clone();
            clone.id = this.id;
            clone.time = (Timestamp) this.time.clone();
            clone.destinationLatitude = this.destinationLatitude;
            clone.destinationLongitude = this.destinationLongitude;
            clone.customerLatitude = this.customerLatitude;
            clone.customerLongitude = this.customerLongitude;
            clone.direction = this.direction;
            clone.customerId = this.customerId;
            clone.driverId = this.driverId;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The WaypointDTO object could not be cloned.", e);
        }
    }
}
